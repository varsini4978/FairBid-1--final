package com.fairbid.service;

import com.fairbid.dao.*;
import com.fairbid.exception.*;
import com.fairbid.interfaces.Biddable;
import com.fairbid.interfaces.Sellable;
import com.fairbid.model.*;
import com.fairbid.util.DatabaseConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Multiple Interface Implementation & Core Auction Engine:
 * Implements Sellable (producer operations) and Biddable (buyer operations).
 * Acts as the centralized transaction coordinator enforcing all business rules:
 * - Concurrency control with row-level locks (SELECT ... FOR UPDATE)
 * - Strict bid price validations (min price snapshot, outbidding rules)
 * - Automatic and lazy expiration management
 * - Idempotent auction closing and winner settlement
 */
public class AuctionManager implements Sellable, Biddable {
    private static final Logger LOGGER = Logger.getLogger(AuctionManager.class.getName());

    private final ProduceLotDAO produceLotDAO;
    private final AuctionDAO auctionDAO;
    private final BidDAO bidDAO;
    private final ResultDAO resultDAO;
    private final AgentOfferDAO agentOfferDAO;

    public AuctionManager() {
        this.produceLotDAO = new ProduceLotDAO();
        this.auctionDAO = new AuctionDAO();
        this.bidDAO = new BidDAO();
        this.resultDAO = new ResultDAO();
        this.agentOfferDAO = new AgentOfferDAO();
    }

    public AuctionManager(ProduceLotDAO produceLotDAO, AuctionDAO auctionDAO,
                          BidDAO bidDAO, ResultDAO resultDAO, AgentOfferDAO agentOfferDAO) {
        this.produceLotDAO = produceLotDAO;
        this.auctionDAO = auctionDAO;
        this.bidDAO = bidDAO;
        this.resultDAO = resultDAO;
        this.agentOfferDAO = agentOfferDAO;
    }

    // -------------------------------------------------------------------------
    // 1. Implementation of Sellable Interface (Producer Operations)
    // -------------------------------------------------------------------------
    @Override
    public ProduceLot listProduce(ProduceLot lot) {
        if (lot.getQuantity() == null || lot.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Produce quantity must be strictly positive");
        }
        if (lot.getMinPricePerUnit() == null || lot.getMinPricePerUnit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Minimum price must be strictly positive");
        }
        lot.setStatus(LotStatus.AVAILABLE);
        return produceLotDAO.create(lot);
    }

    @Override
    public void reviseMinimumPrice(int lotId, int farmerId, BigDecimal newMinPrice) {
        ProduceLot lot = produceLotDAO.findById(lotId);
        if (lot == null) {
            throw new ResourceNotFoundException("Produce lot not found with ID: " + lotId);
        }
        if (lot.getFarmerId() != farmerId) {
            throw new UnauthorizedActionException("You are not authorized to modify another farmer's produce lot");
        }
        if (lot.getStatus() == LotStatus.IN_AUCTION) {
            throw new ValidationException("Cannot modify price while an active auction is running");
        }
        if (newMinPrice == null || newMinPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("New minimum price must be strictly greater than zero");
        }

        produceLotDAO.updateMinPrice(lotId, newMinPrice);
    }

    @Override
    public Auction reopenAuction(int lotId, int farmerId, LocalDateTime newEndTime) {
        ProduceLot lot = produceLotDAO.findById(lotId);
        if (lot == null) {
            throw new ResourceNotFoundException("Produce lot not found with ID: " + lotId);
        }
        if (lot.getFarmerId() != farmerId) {
            throw new UnauthorizedActionException("You are not authorized to reopen another farmer's produce lot");
        }
        if (lot.getStatus() == LotStatus.SOLD || lot.getStatus() == LotStatus.SOLD_TO_AGENT) {
            throw new ValidationException("Cannot reopen a produce lot that has already been sold");
        }
        if (newEndTime == null || !newEndTime.isAfter(LocalDateTime.now())) {
            throw new ValidationException("Auction closing time must be set in the future");
        }

        // Lazy check: close any currently lingering open auction
        Auction existingOpen = auctionDAO.findActiveByLotId(lotId);
        if (existingOpen != null) {
            closeAuction(existingOpen.getId(), farmerId);
        }

        // Determine next round number
        List<Auction> previousRounds = auctionDAO.findByLotId(lotId);
        int nextRound = previousRounds.size() + 1;

        Auction newRound = new Auction();
        newRound.setLotId(lotId);
        newRound.setRoundNumber(nextRound);
        newRound.setMinPriceSnapshot(lot.getMinPricePerUnit());
        newRound.setStartTime(new Timestamp(System.currentTimeMillis()));
        newRound.setEndTime(Timestamp.valueOf(newEndTime));
        newRound.setStatus(AuctionStatus.OPEN);

        Auction created = auctionDAO.create(newRound);
        produceLotDAO.updateStatus(lotId, LotStatus.IN_AUCTION);
        return created;
    }

    @Override
    public void decideOnAgentOffer(int lotId, int farmerId, AgentDecision decision, BigDecimal transportCost, BigDecimal otherExpenses) {
        ProduceLot lot = produceLotDAO.findById(lotId);
        if (lot == null) {
            throw new ResourceNotFoundException("Produce lot not found with ID: " + lotId);
        }
        if (lot.getFarmerId() != farmerId) {
            throw new UnauthorizedActionException("You are not authorized to make decisions on another farmer's produce lot");
        }
        if (decision == null || decision == AgentDecision.PENDING) {
            throw new ValidationException("A definitive decision (ACCEPTED or REJECTED) is required");
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            agentOfferDAO.recordDecision(conn, lotId, decision, transportCost, otherExpenses);

            if (decision == AgentDecision.ACCEPTED) {
                produceLotDAO.updateStatus(conn, lotId, LotStatus.SOLD_TO_AGENT);
            } else {
                produceLotDAO.updateStatus(conn, lotId, LotStatus.UNSOLD);
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            LOGGER.log(Level.SEVERE, "Failed recording farmer agent decision", e);
            throw new FairBidException("Database error recording decision", e, 500);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    // -------------------------------------------------------------------------
    // 2. Implementation of Biddable Interface (Buyer Operations)
    // -------------------------------------------------------------------------
    @Override
    public Bid placeBid(int auctionId, int buyerId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Bid amount must be strictly greater than zero");
        }
        if (amount.scale() > 2) {
            throw new ValidationException("Bid amount cannot have more than 2 decimal places");
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Concurrency Lock: SELECT ... FOR UPDATE
            Auction auction = auctionDAO.findByIdForUpdate(conn, auctionId);
            if (auction == null) {
                throw new ResourceNotFoundException("Auction round not found with ID: " + auctionId);
            }

            // Lazy Expiration Check
            if (auction.isExpired() || auction.getStatus() == AuctionStatus.CLOSED) {
                conn.commit(); // release lock
                closeAuction(auctionId); // lazily close
                throw new AuctionClosedException("Auction is closed. Bids are no longer accepted.");
            }

            // Bid >= Minimum Price Check
            if (amount.compareTo(auction.getMinPriceSnapshot()) < 0) {
                throw new InvalidBidException("Bid amount (₹" + amount + ") must be at least the minimum acceptable price of ₹" + auction.getMinPriceSnapshot());
            }

            // Strictly greater than highest valid bid check
            Bid highestBid = bidDAO.findHighestValidBid(conn, auctionId);
            if (highestBid != null) {
                if (amount.compareTo(highestBid.getAmountPerUnit()) <= 0) {
                    throw new InvalidBidException("Bid amount (₹" + amount + ") must be strictly higher than current highest bid of ₹" + highestBid.getAmountPerUnit());
                }
                // Supersede previous winning bid
                bidDAO.updateBidStatus(conn, highestBid.getId(), BidStatus.OUTBID);
            }

            // Create new winning bid
            Bid newBid = new Bid(0, auctionId, buyerId, amount, new Timestamp(System.currentTimeMillis()), BidStatus.WINNING);
            Bid createdBid = bidDAO.create(conn, newBid);

            conn.commit();
            return createdBid;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            LOGGER.log(Level.SEVERE, "Transaction rollback during bid placement", e);
            throw new FairBidException("Database error placing bid", e, 500);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public List<Bid> getBidHistory(int buyerId) {
        return bidDAO.findBidsByBuyerId(buyerId);
    }

    // -------------------------------------------------------------------------
    // 3. Auction Lifecycle Management (Method Overloading)
    // -------------------------------------------------------------------------
    public Auction startAuction(int lotId, int farmerId, LocalDateTime endTime) {
        ProduceLot lot = produceLotDAO.findById(lotId);
        if (lot == null) {
            throw new ResourceNotFoundException("Produce lot not found with ID: " + lotId);
        }
        if (lot.getFarmerId() != farmerId) {
            throw new UnauthorizedActionException("You can only start auctions for your own produce");
        }
        if (lot.getStatus() == LotStatus.IN_AUCTION) {
            throw new ValidationException("An auction is already currently open for this produce lot");
        }
        if (lot.getStatus() == LotStatus.SOLD || lot.getStatus() == LotStatus.SOLD_TO_AGENT) {
            throw new ValidationException("Cannot auction a lot that has already been sold");
        }
        if (endTime == null || !endTime.isAfter(LocalDateTime.now())) {
            throw new ValidationException("Auction closing time must be set in the future");
        }

        List<Auction> previous = auctionDAO.findByLotId(lotId);
        int roundNumber = previous.size() + 1;

        Auction auction = new Auction();
        auction.setLotId(lotId);
        auction.setRoundNumber(roundNumber);
        auction.setMinPriceSnapshot(lot.getMinPricePerUnit());
        auction.setStartTime(new Timestamp(System.currentTimeMillis()));
        auction.setEndTime(Timestamp.valueOf(endTime));
        auction.setStatus(AuctionStatus.OPEN);

        Auction created = auctionDAO.create(auction);
        produceLotDAO.updateStatus(lotId, LotStatus.IN_AUCTION);
        return created;
    }

    /**
     * Method Overloading: Closes auction without farmer ownership check (system auto-close).
     */
    public AuctionResult closeAuction(int auctionId) {
        return closeAuction(auctionId, null);
    }

    /**
     * Method Overloading: Closes auction with optional farmer ownership verification (early close).
     * Idempotent: Subsequent calls return existing result without duplicate rows.
     */
    public AuctionResult closeAuction(int auctionId, Integer farmerIdOpt) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            Auction auction = auctionDAO.findByIdForUpdate(conn, auctionId);
            if (auction == null) {
                throw new ResourceNotFoundException("Auction not found with ID: " + auctionId);
            }

            // Check farmer ownership if closed by farmer explicitly
            if (farmerIdOpt != null && auction.getProduceLot().getFarmerId() != farmerIdOpt) {
                throw new UnauthorizedActionException("You are not authorized to close another farmer's auction");
            }

            // Idempotency: If already closed, return existing result
            if (auction.getStatus() == AuctionStatus.CLOSED) {
                conn.commit();
                return resultDAO.findByAuctionId(auctionId);
            }

            // Find winning bid: Highest valid bid >= min_price_snapshot
            Bid winningBid = bidDAO.findHighestValidBid(conn, auctionId);
            AuctionResult result = new AuctionResult();
            result.setAuctionId(auctionId);
            result.setClosedAt(new Timestamp(System.currentTimeMillis()));

            if (winningBid != null && winningBid.getAmountPerUnit().compareTo(auction.getMinPriceSnapshot()) >= 0) {
                // Winner found!
                result.setOutcome(AuctionOutcome.SOLD);
                result.setWinningBidId(winningBid.getId());
                result.setWinnerBuyerId(winningBid.getBuyerId());
                result.setFinalPricePerUnit(winningBid.getAmountPerUnit());

                BigDecimal total = auction.getProduceLot().getQuantity().multiply(winningBid.getAmountPerUnit())
                        .setScale(2, RoundingMode.HALF_UP);
                result.setTotalAmount(total);

                auctionDAO.closeAuction(conn, auctionId, AuctionOutcome.SOLD);
                produceLotDAO.updateStatus(conn, auction.getLotId(), LotStatus.SOLD);
            } else {
                // No valid bids received -> UNSOLD
                result.setOutcome(AuctionOutcome.UNSOLD);
                result.setWinningBidId(null);
                result.setWinnerBuyerId(null);
                result.setFinalPricePerUnit(null);
                result.setTotalAmount(null);

                auctionDAO.closeAuction(conn, auctionId, AuctionOutcome.UNSOLD);
                produceLotDAO.updateStatus(conn, auction.getLotId(), LotStatus.UNSOLD);
            }

            AuctionResult saved = resultDAO.create(conn, result);
            conn.commit();
            return saved;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            LOGGER.log(Level.SEVERE, "Transaction rollback closing auction: " + auctionId, e);
            throw new FairBidException("Database error closing auction", e, 500);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Lazy check: Evaluates whether an auction has expired upon reading and closes it if needed.
     */
    public void checkAndCloseIfExpired(int auctionId) {
        Auction auction = auctionDAO.findById(auctionId);
        if (auction != null && auction.getStatus() == AuctionStatus.OPEN && auction.isExpired()) {
            closeAuction(auctionId);
        }
    }

    /**
     * Periodic background closing executed by AuctionSchedulerListener.
     */
    public void closeAllExpiredAuctions() {
        List<Auction> expired = auctionDAO.findExpiredOpenAuctions();
        for (Auction a : expired) {
            try {
                closeAuction(a.getId());
                LOGGER.info("Auto-closed expired auction ID: " + a.getId());
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed auto-closing auction ID: " + a.getId(), e);
            }
        }
    }

    public Auction getAuctionDetails(int auctionId) {
        checkAndCloseIfExpired(auctionId);
        Auction auction = auctionDAO.findById(auctionId);
        if (auction != null) {
            List<Bid> bids = bidDAO.findBidsByAuctionId(auctionId);
            auction.setBids(bids);
            auction.setHighestBid(auction.determineHighestValidBid());
        }
        return auction;
    }
}

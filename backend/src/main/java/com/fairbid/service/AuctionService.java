package com.fairbid.service;

import com.fairbid.dao.*;
import com.fairbid.exception.ResourceNotFoundException;
import com.fairbid.model.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service orchestrating query views for produce lots, open auctions, round histories, and earnings.
 */
public class AuctionService {
    private final ProduceLotDAO produceLotDAO;
    private final AuctionDAO auctionDAO;
    private final BidDAO bidDAO;
    private final ResultDAO resultDAO;
    private final AgentOfferDAO agentOfferDAO;
    private final AuctionManager auctionManager;

    public AuctionService() {
        this.produceLotDAO = new ProduceLotDAO();
        this.auctionDAO = new AuctionDAO();
        this.bidDAO = new BidDAO();
        this.resultDAO = new ResultDAO();
        this.agentOfferDAO = new AgentOfferDAO();
        this.auctionManager = new AuctionManager(produceLotDAO, auctionDAO, bidDAO, resultDAO, agentOfferDAO);
    }

    public AuctionManager getAuctionManager() {
        return auctionManager;
    }

    public List<ProduceLot> getFarmerLots(int farmerId) {
        return produceLotDAO.findByFarmerId(farmerId);
    }

    public ProduceLot getProduceLot(int lotId) {
        ProduceLot lot = produceLotDAO.findById(lotId);
        if (lot == null) {
            throw new ResourceNotFoundException("Produce lot not found with ID: " + lotId);
        }
        return lot;
    }

    public List<Auction> getOpenAuctions(String category, String search) {
        List<Auction> open = auctionDAO.findAllOpen(category, search);
        for (Auction a : open) {
            List<Bid> bids = bidDAO.findBidsByAuctionId(a.getId());
            a.setBids(bids);
            a.setHighestBid(a.determineHighestValidBid());
        }
        return open;
    }

    public List<ProduceLot> browseProduce(String category, String search) {
        return produceLotDAO.findAll(category, search);
    }

    public List<Auction> getLotRounds(int lotId) {
        List<Auction> rounds = auctionDAO.findByLotId(lotId);
        for (Auction a : rounds) {
            List<Bid> bids = bidDAO.findBidsByAuctionId(a.getId());
            a.setBids(bids);
            a.setHighestBid(a.determineHighestValidBid());
        }
        return rounds;
    }

    public AuctionResult getAuctionResult(int auctionId) {
        // Lazy close check first
        auctionManager.checkAndCloseIfExpired(auctionId);
        AuctionResult result = resultDAO.findByAuctionId(auctionId);
        if (result == null) {
            // If auction was closed without result, create it now
            Auction a = auctionDAO.findById(auctionId);
            if (a != null && a.getStatus() == AuctionStatus.CLOSED) {
                result = auctionManager.closeAuction(auctionId);
            }
        }
        return result;
    }

    public List<AuctionResult> getWonAuctions(int buyerId) {
        return resultDAO.findWonAuctionsByBuyerId(buyerId);
    }

    public BigDecimal getFarmerEarnings(int farmerId) {
        return produceLotDAO.getFarmerTotalEarnings(farmerId);
    }

    public AgentOffer getAgentOffer(int lotId) {
        return agentOfferDAO.findByLotId(lotId);
    }

    public AuctionResult getLatestResultByLotId(int lotId) {
        return resultDAO.findLatestResultByLotId(lotId);
    }
}

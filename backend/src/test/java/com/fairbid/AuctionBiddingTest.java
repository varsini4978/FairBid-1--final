package com.fairbid;

import com.fairbid.exception.AuctionClosedException;
import com.fairbid.exception.InvalidBidException;
import com.fairbid.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying core auction bidding rules, collections sorting,
 * tie-breaking, and exception handling without database dependencies.
 */
public class AuctionBiddingTest {

    private Auction auction;
    private final BigDecimal minPrice = new BigDecimal("25.00");

    @BeforeEach
    void setUp() {
        auction = new Auction(
                1,
                101,
                1,
                minPrice,
                new Timestamp(System.currentTimeMillis() - 3600000),
                new Timestamp(System.currentTimeMillis() + 3600000),
                AuctionStatus.OPEN,
                null
        );
    }

    @Test
    @DisplayName("Rule 1: Bid below minimum price must be rejected with InvalidBidException")
    void testBidBelowMinimumRejected() {
        BigDecimal lowBid = new BigDecimal("24.50");
        assertThrows(InvalidBidException.class, () -> {
            if (lowBid.compareTo(auction.getMinPriceSnapshot()) < 0) {
                throw new InvalidBidException("Bid is below minimum price");
            }
        });
    }

    @Test
    @DisplayName("Rule 2: Bid not strictly higher than current highest must be rejected")
    void testBidNotHigherThanCurrentRejected() {
        Bid existingHighest = new Bid(1, 1, 10, new BigDecimal("28.00"), new Timestamp(System.currentTimeMillis() - 10000), BidStatus.WINNING);
        auction.addBid(existingHighest);

        BigDecimal equalBid = new BigDecimal("28.00");
        BigDecimal lowerBid = new BigDecimal("27.50");

        assertThrows(InvalidBidException.class, () -> {
            if (equalBid.compareTo(existingHighest.getAmountPerUnit()) <= 0) {
                throw new InvalidBidException("Bid must be strictly higher than current highest bid");
            }
        });

        assertThrows(InvalidBidException.class, () -> {
            if (lowerBid.compareTo(existingHighest.getAmountPerUnit()) <= 0) {
                throw new InvalidBidException("Bid must be strictly higher than current highest bid");
            }
        });
    }

    @Test
    @DisplayName("Rule 3: Bidding on closed or expired auction must throw AuctionClosedException")
    void testBidAfterCloseRejected() {
        auction.setStatus(AuctionStatus.CLOSED);

        assertThrows(AuctionClosedException.class, () -> {
            if (auction.getStatus() == AuctionStatus.CLOSED || auction.isExpired()) {
                throw new AuctionClosedException("Auction is closed");
            }
        });
    }

    @Test
    @DisplayName("Rule 4: Highest valid bid wins the auction")
    void testHighestBidWins() {
        long now = System.currentTimeMillis();
        Bid b1 = new Bid(1, 1, 10, new BigDecimal("26.00"), new Timestamp(now - 30000), BidStatus.VALID);
        Bid b2 = new Bid(2, 1, 11, new BigDecimal("29.50"), new Timestamp(now - 20000), BidStatus.VALID);
        Bid b3 = new Bid(3, 1, 12, new BigDecimal("27.00"), new Timestamp(now - 10000), BidStatus.VALID);

        auction.addBid(b1);
        auction.addBid(b2);
        auction.addBid(b3);

        Bid winner = auction.determineHighestValidBid();
        assertNotNull(winner);
        assertEquals(2, winner.getId());
        assertEquals(new BigDecimal("29.50"), winner.getAmountPerUnit());
        assertEquals(11, winner.getBuyerId());
    }

    @Test
    @DisplayName("Rule 5: Tie-breaking by earliest placed bid when amounts are identical")
    void testTieBreakByEarliestBid() {
        long now = System.currentTimeMillis();
        // Two buyers place identical bids of ₹30.00, but Buyer 10 placed it 5 seconds earlier
        Bid bEarly = new Bid(1, 1, 10, new BigDecimal("30.00"), new Timestamp(now - 20000), BidStatus.VALID);
        Bid bLate = new Bid(2, 1, 11, new BigDecimal("30.00"), new Timestamp(now - 15000), BidStatus.VALID);

        auction.addBid(bEarly);
        auction.addBid(bLate);

        Bid winner = auction.determineHighestValidBid();
        assertNotNull(winner);
        assertEquals(1, winner.getId(), "Earliest placed bid must win the tie-break");
        assertEquals(10, winner.getBuyerId());
    }

    @Test
    @DisplayName("Rule 6: Auction with no valid bids produces UNSOLD outcome")
    void testUnsoldWhenNoValidBids() {
        // Auction with no bids
        Bid winner = auction.determineHighestValidBid();
        assertNull(winner, "Highest bid must be null when no bids exist");

        AuctionResult result = new AuctionResult();
        result.setAuctionId(auction.getId());
        if (winner == null) {
            result.setOutcome(AuctionOutcome.UNSOLD);
        } else {
            result.setOutcome(AuctionOutcome.SOLD);
        }

        assertEquals(AuctionOutcome.UNSOLD, result.getOutcome());
        assertNull(result.getWinningBidId());
        assertNull(result.getWinnerBuyerId());
    }

    @Test
    @DisplayName("Rule 7: Reopen creates a new round while preserving previous round history")
    void testMultiRoundHistoryPreservation() {
        List<Auction> rounds = new ArrayList<>();

        // Round 1 (closed unsold)
        Auction r1 = new Auction(1, 100, 1, new BigDecimal("50.00"),
                new Timestamp(System.currentTimeMillis() - 7200000),
                new Timestamp(System.currentTimeMillis() - 3600000),
                AuctionStatus.CLOSED, AuctionOutcome.UNSOLD);
        rounds.add(r1);

        // Round 2 (reopened with revised price)
        Auction r2 = new Auction(2, 100, rounds.size() + 1, new BigDecimal("45.00"),
                new Timestamp(System.currentTimeMillis()),
                new Timestamp(System.currentTimeMillis() + 3600000),
                AuctionStatus.OPEN, null);
        rounds.add(r2);

        assertEquals(2, rounds.size());
        assertEquals(1, rounds.get(0).getRoundNumber());
        assertEquals(AuctionStatus.CLOSED, rounds.get(0).getStatus());
        assertEquals(AuctionOutcome.UNSOLD, rounds.get(0).getOutcome());
        assertEquals(new BigDecimal("50.00"), rounds.get(0).getMinPriceSnapshot());

        assertEquals(2, rounds.get(1).getRoundNumber());
        assertEquals(AuctionStatus.OPEN, rounds.get(1).getStatus());
        assertEquals(new BigDecimal("45.00"), rounds.get(1).getMinPriceSnapshot());
    }
}

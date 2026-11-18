package com.fairbid.model;

import com.fairbid.exception.ValidationException;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Encapsulation & Collections:
 * Domain model representing an auction round for a produce lot.
 * Maintains an internal ArrayList of bids and uses Java Collections/Comparators
 * to determine the winning bid based on highest price and earliest timestamp.
 */
public class Auction {
    private int id;
    private int lotId;
    private int roundNumber;
    private BigDecimal minPriceSnapshot;
    private Timestamp startTime;
    private Timestamp endTime;
    private AuctionStatus status;
    private AuctionOutcome outcome;

    // Collections: ArrayList of bids placed in this auction
    private final List<Bid> bids = new ArrayList<>();

    // Transient references for UI presentation
    private ProduceLot produceLot;
    private Bid highestBid;

    public Auction() {
        this.roundNumber = 1;
        this.status = AuctionStatus.OPEN;
    }

    public Auction(int id, int lotId, int roundNumber, BigDecimal minPriceSnapshot,
                   Timestamp startTime, Timestamp endTime, AuctionStatus status, AuctionOutcome outcome) {
        setId(id);
        setLotId(lotId);
        setRoundNumber(roundNumber);
        setMinPriceSnapshot(minPriceSnapshot);
        this.startTime = (startTime != null) ? startTime : new Timestamp(System.currentTimeMillis());
        setEndTime(endTime);
        this.status = (status != null) ? status : AuctionStatus.OPEN;
        this.outcome = outcome;
    }

    // -------------------------------------------------------------------------
    // Collections & Business Rule Logic
    // -------------------------------------------------------------------------
    public void addBid(Bid bid) {
        if (bid != null) {
            this.bids.add(bid);
        }
    }

    public void setBids(List<Bid> bids) {
        this.bids.clear();
        if (bids != null) {
            this.bids.addAll(bids);
        }
    }

    public List<Bid> getBids() {
        return Collections.unmodifiableList(bids);
    }

    /**
     * Determines the current highest valid bid using Collections Comparator.
     * Rule: Highest amount wins; tie broken by EARLIEST placed_at timestamp.
     */
    public Bid determineHighestValidBid() {
        return bids.stream()
                .filter(b -> b.getStatus() == BidStatus.VALID || b.getStatus() == BidStatus.WINNING || b.getStatus() == BidStatus.OUTBID)
                .filter(b -> b.getAmountPerUnit().compareTo(minPriceSnapshot) >= 0)
                .min(Comparator.comparing(Bid::getAmountPerUnit).reversed()
                        .thenComparing(Bid::getPlacedAt))
                .orElse(null);
    }

    public long getValidBidCount() {
        return bids.stream()
                .filter(b -> b.getStatus() != BidStatus.REJECTED)
                .count();
    }

    public boolean isExpired() {
        return endTime != null && System.currentTimeMillis() >= endTime.getTime();
    }

    public long getTimeRemainingSeconds() {
        if (endTime == null) return 0;
        long diff = endTime.getTime() - System.currentTimeMillis();
        return Math.max(0, diff / 1000);
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------
    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) throw new ValidationException("Auction ID cannot be negative");
        this.id = id;
    }

    public int getLotId() {
        return lotId;
    }

    public void setLotId(int lotId) {
        if (lotId <= 0) throw new ValidationException("Lot ID must be positive");
        this.lotId = lotId;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        if (roundNumber <= 0) throw new ValidationException("Round number must be at least 1");
        this.roundNumber = roundNumber;
    }

    public BigDecimal getMinPriceSnapshot() {
        return minPriceSnapshot;
    }

    public void setMinPriceSnapshot(BigDecimal minPriceSnapshot) {
        if (minPriceSnapshot == null || minPriceSnapshot.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Minimum price snapshot must be greater than zero");
        }
        this.minPriceSnapshot = minPriceSnapshot;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        if (endTime == null) {
            throw new ValidationException("Auction closing time is required");
        }
        this.endTime = endTime;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public AuctionOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(AuctionOutcome outcome) {
        this.outcome = outcome;
    }

    public ProduceLot getProduceLot() {
        return produceLot;
    }

    public void setProduceLot(ProduceLot produceLot) {
        this.produceLot = produceLot;
    }

    public Bid getHighestBid() {
        return highestBid;
    }

    public void setHighestBid(Bid highestBid) {
        this.highestBid = highestBid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Auction auction)) return false;
        return id == auction.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Auction{" +
                "id=" + id +
                ", lotId=" + lotId +
                ", roundNumber=" + roundNumber +
                ", minPriceSnapshot=" + minPriceSnapshot +
                ", status=" + status +
                ", outcome=" + outcome +
                '}';
    }
}

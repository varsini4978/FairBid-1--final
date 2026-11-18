package com.fairbid.model;

import com.fairbid.exception.ValidationException;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Encapsulation:
 * Domain model representing settled outcomes of an auction.
 */
public class AuctionResult {
    private int id;
    private int auctionId;
    private Integer winningBidId;
    private Integer winnerBuyerId;
    private BigDecimal finalPricePerUnit;
    private BigDecimal totalAmount;
    private AuctionOutcome outcome;
    private Timestamp closedAt;

    // Transient presentation fields
    private int lotId;                        // ← needed for recovery-page links
    private BigDecimal minPriceSnapshot;      // ← auction's reserve price
    private BigDecimal agentOfferPerUnit;     // ← agent offer on this lot (may be 0)
    private int totalBids;                    // ← number of bids placed
    private String winnerName;
    private String winnerBusiness;
    private String cropName;
    private BigDecimal quantity;
    private String unit;
    private int roundNumber;

    public AuctionResult() {
    }

    public AuctionResult(int id, int auctionId, Integer winningBidId, Integer winnerBuyerId,
                         BigDecimal finalPricePerUnit, BigDecimal totalAmount, AuctionOutcome outcome, Timestamp closedAt) {
        setId(id);
        setAuctionId(auctionId);
        this.winningBidId = winningBidId;
        this.winnerBuyerId = winnerBuyerId;
        this.finalPricePerUnit = finalPricePerUnit;
        this.totalAmount = totalAmount;
        this.outcome = outcome;
        this.closedAt = (closedAt != null) ? closedAt : new Timestamp(System.currentTimeMillis());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) throw new ValidationException("Result ID cannot be negative");
        this.id = id;
    }

    public int getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(int auctionId) {
        if (auctionId <= 0) throw new ValidationException("Auction ID must be positive");
        this.auctionId = auctionId;
    }

    public Integer getWinningBidId() {
        return winningBidId;
    }

    public void setWinningBidId(Integer winningBidId) {
        this.winningBidId = winningBidId;
    }

    public Integer getWinnerBuyerId() {
        return winnerBuyerId;
    }

    public void setWinnerBuyerId(Integer winnerBuyerId) {
        this.winnerBuyerId = winnerBuyerId;
    }

    public BigDecimal getFinalPricePerUnit() {
        return finalPricePerUnit;
    }

    public void setFinalPricePerUnit(BigDecimal finalPricePerUnit) {
        this.finalPricePerUnit = finalPricePerUnit;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public AuctionOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(AuctionOutcome outcome) {
        this.outcome = outcome;
    }

    public Timestamp getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Timestamp closedAt) {
        this.closedAt = closedAt;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public void setWinnerName(String winnerName) {
        this.winnerName = winnerName;
    }

    public String getWinnerBusiness() {
        return winnerBusiness;
    }

    public void setWinnerBusiness(String winnerBusiness) {
        this.winnerBusiness = winnerBusiness;
    }

    public String getCropName() {
        return cropName;
    }

    public void setCropName(String cropName) {
        this.cropName = cropName;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public int getLotId() { return lotId; }
    public void setLotId(int lotId) { this.lotId = lotId; }

    public BigDecimal getMinPriceSnapshot() { return minPriceSnapshot; }
    public void setMinPriceSnapshot(BigDecimal minPriceSnapshot) { this.minPriceSnapshot = minPriceSnapshot; }

    public BigDecimal getAgentOfferPerUnit() { return agentOfferPerUnit; }
    public void setAgentOfferPerUnit(BigDecimal agentOfferPerUnit) { this.agentOfferPerUnit = agentOfferPerUnit; }

    public int getTotalBids() { return totalBids; }
    public void setTotalBids(int totalBids) { this.totalBids = totalBids; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuctionResult that)) return false;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AuctionResult{" +
                "id=" + id +
                ", auctionId=" + auctionId +
                ", outcome=" + outcome +
                ", finalPrice=" + finalPricePerUnit +
                ", totalAmount=" + totalAmount +
                '}';
    }
}

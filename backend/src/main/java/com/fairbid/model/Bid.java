package com.fairbid.model;

import com.fairbid.exception.ValidationException;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Encapsulation:
 * Domain model representing a bid submitted by a buyer.
 */
public class Bid {
    private int id;
    private int auctionId;
    private int buyerId;
    private BigDecimal amountPerUnit;
    private Timestamp placedAt;
    private BidStatus status;

    // Transient presentation fields
    private String buyerName;
    private String buyerBusiness;

    public Bid() {
        this.status = BidStatus.VALID;
    }

    public Bid(int id, int auctionId, int buyerId, BigDecimal amountPerUnit, Timestamp placedAt, BidStatus status) {
        setId(id);
        setAuctionId(auctionId);
        setBuyerId(buyerId);
        setAmountPerUnit(amountPerUnit);
        this.placedAt = (placedAt != null) ? placedAt : new Timestamp(System.currentTimeMillis());
        this.status = (status != null) ? status : BidStatus.VALID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) throw new ValidationException("Bid ID cannot be negative");
        this.id = id;
    }

    public int getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(int auctionId) {
        if (auctionId <= 0) throw new ValidationException("Auction ID must be positive");
        this.auctionId = auctionId;
    }

    public int getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(int buyerId) {
        if (buyerId <= 0) throw new ValidationException("Buyer ID must be positive");
        this.buyerId = buyerId;
    }

    public BigDecimal getAmountPerUnit() {
        return amountPerUnit;
    }

    public void setAmountPerUnit(BigDecimal amountPerUnit) {
        if (amountPerUnit == null || amountPerUnit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Bid amount must be greater than zero");
        }
        if (amountPerUnit.scale() > 2) {
            throw new ValidationException("Bid amount cannot have more than 2 decimal places");
        }
        this.amountPerUnit = amountPerUnit;
    }

    public Timestamp getPlacedAt() {
        return placedAt;
    }

    public void setPlacedAt(Timestamp placedAt) {
        this.placedAt = placedAt;
    }

    public BidStatus getStatus() {
        return status;
    }

    public void setStatus(BidStatus status) {
        this.status = status;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getBuyerBusiness() {
        return buyerBusiness;
    }

    public void setBuyerBusiness(String buyerBusiness) {
        this.buyerBusiness = buyerBusiness;
    }

    /**
     * Privacy requirement: Mask other buyers' identities for competing buyers.
     * e.g., "Buyer #A12"
     */
    public String getMaskedBuyerName() {
        return String.format("Buyer #B%03d", buyerId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bid bid)) return false;
        return id == bid.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Bid{" +
                "id=" + id +
                ", auctionId=" + auctionId +
                ", buyerId=" + buyerId +
                ", amount=" + amountPerUnit +
                ", placedAt=" + placedAt +
                ", status=" + status +
                '}';
    }
}

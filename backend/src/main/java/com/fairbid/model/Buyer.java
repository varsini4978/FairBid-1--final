package com.fairbid.model;

import com.fairbid.exception.ValidationException;
import com.fairbid.interfaces.Biddable;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

/**
 * Inheritance & Polymorphism:
 * Buyer represents commercial buyers, wholesalers, and food processors.
 * Inherits core identity and security fields from User using super(...).
 * Implements Biddable to encapsulate buyer bidding actions.
 */
public class Buyer extends User implements Biddable {
    private String businessName;
    private String buyerType;

    // Optional reference to engine for domain delegation
    private transient Biddable engine;

    public Buyer() {
        super();
    }

    public Buyer(int id, String fullName, String email, String passwordHash, String phone, String location,
                 Timestamp createdAt, String businessName, String buyerType) {
        super(id, fullName, email, passwordHash, phone, location, createdAt);
        setBusinessName(businessName);
        setBuyerType(buyerType);
    }

    public void setEngine(Biddable engine) {
        this.engine = engine;
    }

    // -------------------------------------------------------------------------
    // Polymorphic Overrides of Abstract User Methods
    // -------------------------------------------------------------------------
    @Override
    public Role getRole() {
        return Role.BUYER;
    }

    @Override
    public String getDashboardPath() {
        return "buyer-dashboard.html";
    }

    @Override
    public String getProfileSummary() {
        return String.format("Buyer: %s | Business: %s | Category: %s",
                getFullName(), businessName, buyerType);
    }

    // -------------------------------------------------------------------------
    // Implementation of Biddable Interface (Domain Delegation)
    // -------------------------------------------------------------------------
    @Override
    public Bid placeBid(int auctionId, int buyerId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Bid amount must be a positive number");
        }
        if (amount.scale() > 2) {
            throw new ValidationException("Bid amount cannot have more than 2 decimal places");
        }
        if (engine != null) {
            return engine.placeBid(auctionId, this.getId(), amount);
        }
        return new Bid(0, auctionId, this.getId(), amount, new Timestamp(System.currentTimeMillis()), BidStatus.VALID);
    }

    @Override
    public List<Bid> getBidHistory(int buyerId) {
        if (engine != null) {
            return engine.getBidHistory(this.getId());
        }
        return Collections.emptyList();
    }

    // -------------------------------------------------------------------------
    // Encapsulation Getters and Setters
    // -------------------------------------------------------------------------
    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = (businessName != null) ? businessName.trim() : "";
    }

    public String getBuyerType() {
        return buyerType;
    }

    public void setBuyerType(String buyerType) {
        this.buyerType = (buyerType != null) ? buyerType.trim() : "";
    }

    @Override
    public String toString() {
        return "Buyer{" +
                "id=" + getId() +
                ", fullName='" + getFullName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", businessName='" + businessName + '\'' +
                ", buyerType='" + buyerType + '\'' +
                '}';
    }
}

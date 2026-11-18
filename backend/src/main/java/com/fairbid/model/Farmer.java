package com.fairbid.model;

import com.fairbid.exception.ValidationException;
import com.fairbid.interfaces.Sellable;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Inheritance & Polymorphism:
 * Farmer represents agricultural producers on the FairBid platform.
 * Inherits core identity and security fields from User using super(...).
 * Implements Sellable to encapsulate domain-level producer actions.
 */
public class Farmer extends User implements Sellable {
    private String farmName;
    private String village;
    private String district;

    // Optional reference to engine for domain delegation
    private transient Sellable engine;

    public Farmer() {
        super();
    }

    public Farmer(int id, String fullName, String email, String passwordHash, String phone, String location,
                  Timestamp createdAt, String farmName, String village, String district) {
        super(id, fullName, email, passwordHash, phone, location, createdAt);
        setFarmName(farmName);
        setVillage(village);
        setDistrict(district);
    }

    public void setEngine(Sellable engine) {
        this.engine = engine;
    }

    // -------------------------------------------------------------------------
    // Polymorphic Overrides of Abstract User Methods
    // -------------------------------------------------------------------------
    @Override
    public Role getRole() {
        return Role.FARMER;
    }

    @Override
    public String getDashboardPath() {
        return "farmer-dashboard.html";
    }

    @Override
    public String getProfileSummary() {
        return String.format("Farmer: %s | Farm: %s | Village: %s, District: %s",
                getFullName(), farmName, village, district);
    }

    // -------------------------------------------------------------------------
    // Implementation of Sellable Interface (Domain Delegation)
    // -------------------------------------------------------------------------
    @Override
    public ProduceLot listProduce(ProduceLot lot) {
        if (lot == null) {
            throw new ValidationException("Produce lot cannot be null");
        }
        // Enforce ownership
        lot.setFarmerId(this.getId());
        if (engine != null) {
            return engine.listProduce(lot);
        }
        return lot;
    }

    @Override
    public void reviseMinimumPrice(int lotId, int farmerId, BigDecimal newMinPrice) {
        if (newMinPrice == null || newMinPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Revised minimum price must be strictly greater than zero");
        }
        if (engine != null) {
            engine.reviseMinimumPrice(lotId, this.getId(), newMinPrice);
        }
    }

    @Override
    public Auction reopenAuction(int lotId, int farmerId, LocalDateTime newEndTime) {
        if (newEndTime == null || !newEndTime.isAfter(LocalDateTime.now())) {
            throw new ValidationException("Auction closing time must be set in the future");
        }
        if (engine != null) {
            return engine.reopenAuction(lotId, this.getId(), newEndTime);
        }
        return null;
    }

    @Override
    public void decideOnAgentOffer(int lotId, int farmerId, AgentDecision decision, BigDecimal transportCost, BigDecimal otherExpenses) {
        if (decision == null) {
            throw new ValidationException("Farmer decision must be specified");
        }
        if (engine != null) {
            engine.decideOnAgentOffer(lotId, this.getId(), decision, transportCost, otherExpenses);
        }
    }

    // -------------------------------------------------------------------------
    // Encapsulation Getters and Setters
    // -------------------------------------------------------------------------
    public String getFarmName() {
        return farmName;
    }

    public void setFarmName(String farmName) {
        this.farmName = (farmName != null) ? farmName.trim() : "";
    }

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = (village != null) ? village.trim() : "";
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = (district != null) ? district.trim() : "";
    }

    @Override
    public String toString() {
        return "Farmer{" +
                "id=" + getId() +
                ", fullName='" + getFullName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", farmName='" + farmName + '\'' +
                ", district='" + district + '\'' +
                '}';
    }
}

package com.fairbid.model;

import com.fairbid.exception.ValidationException;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Encapsulation:
 * Model representing an external offline agent offer and farmer decision log.
 */
public class AgentOffer {
    private int id;
    private int lotId;
    private BigDecimal offeredPricePerUnit;
    private AgentDecision farmerDecision;
    private BigDecimal transportCost;
    private BigDecimal otherExpenses;
    private Timestamp decidedAt;

    // Transient fields
    private String cropName;
    private BigDecimal quantity;
    private String unit;

    public AgentOffer() {
        this.farmerDecision = AgentDecision.PENDING;
        this.transportCost = BigDecimal.ZERO;
        this.otherExpenses = BigDecimal.ZERO;
    }

    public AgentOffer(int id, int lotId, BigDecimal offeredPricePerUnit, AgentDecision farmerDecision,
                      BigDecimal transportCost, BigDecimal otherExpenses, Timestamp decidedAt) {
        setId(id);
        setLotId(lotId);
        setOfferedPricePerUnit(offeredPricePerUnit);
        this.farmerDecision = (farmerDecision != null) ? farmerDecision : AgentDecision.PENDING;
        this.transportCost = (transportCost != null) ? transportCost : BigDecimal.ZERO;
        this.otherExpenses = (otherExpenses != null) ? otherExpenses : BigDecimal.ZERO;
        this.decidedAt = decidedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) throw new ValidationException("Offer ID cannot be negative");
        this.id = id;
    }

    public int getLotId() {
        return lotId;
    }

    public void setLotId(int lotId) {
        if (lotId <= 0) throw new ValidationException("Lot ID must be positive");
        this.lotId = lotId;
    }

    public BigDecimal getOfferedPricePerUnit() {
        return offeredPricePerUnit;
    }

    public void setOfferedPricePerUnit(BigDecimal offeredPricePerUnit) {
        if (offeredPricePerUnit == null || offeredPricePerUnit.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Offered price cannot be negative");
        }
        this.offeredPricePerUnit = offeredPricePerUnit;
    }

    public AgentDecision getFarmerDecision() {
        return farmerDecision;
    }

    public void setFarmerDecision(AgentDecision farmerDecision) {
        this.farmerDecision = farmerDecision;
    }

    public BigDecimal getTransportCost() {
        return transportCost;
    }

    public void setTransportCost(BigDecimal transportCost) {
        this.transportCost = (transportCost != null) ? transportCost : BigDecimal.ZERO;
    }

    public BigDecimal getOtherExpenses() {
        return otherExpenses;
    }

    public void setOtherExpenses(BigDecimal otherExpenses) {
        this.otherExpenses = (otherExpenses != null) ? otherExpenses : BigDecimal.ZERO;
    }

    public Timestamp getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(Timestamp decidedAt) {
        this.decidedAt = decidedAt;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AgentOffer that)) return false;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AgentOffer{" +
                "id=" + id +
                ", lotId=" + lotId +
                ", offeredPrice=" + offeredPricePerUnit +
                ", decision=" + farmerDecision +
                '}';
    }
}

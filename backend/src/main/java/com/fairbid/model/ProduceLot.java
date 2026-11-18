package com.fairbid.model;

import com.fairbid.exception.ValidationException;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Encapsulation:
 * Domain model representing an agricultural produce lot offered by a farmer.
 */
public class ProduceLot {
    private int id;
    private int farmerId;
    private String cropName;
    private String category;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal minPricePerUnit;
    private BigDecimal agentOfferPerUnit;
    private LotStatus status;
    private Timestamp createdAt;

    // Transient presentation fields for UI display
    private String farmerName;
    private String farmerLocation;
    private Integer activeAuctionId;
    private BigDecimal currentHighestBid;

    public ProduceLot() {
        this.status = LotStatus.AVAILABLE;
        this.agentOfferPerUnit = BigDecimal.ZERO;
    }

    public ProduceLot(int id, int farmerId, String cropName, String category, BigDecimal quantity,
                      String unit, BigDecimal minPricePerUnit, BigDecimal agentOfferPerUnit,
                      LotStatus status, Timestamp createdAt) {
        setId(id);
        setFarmerId(farmerId);
        setCropName(cropName);
        setCategory(category);
        setQuantity(quantity);
        setUnit(unit);
        setMinPricePerUnit(minPricePerUnit);
        setAgentOfferPerUnit(agentOfferPerUnit);
        this.status = (status != null) ? status : LotStatus.AVAILABLE;
        this.createdAt = (createdAt != null) ? createdAt : new Timestamp(System.currentTimeMillis());
    }

    // -------------------------------------------------------------------------
    // Getters & Setters with Business Validation
    // -------------------------------------------------------------------------
    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) throw new ValidationException("Lot ID cannot be negative");
        this.id = id;
    }

    public int getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(int farmerId) {
        if (farmerId <= 0) throw new ValidationException("Valid farmer ID is required");
        this.farmerId = farmerId;
    }

    public String getCropName() {
        return cropName;
    }

    public void setCropName(String cropName) {
        if (cropName == null || cropName.trim().isEmpty()) {
            throw new ValidationException("Crop name is required");
        }
        this.cropName = cropName.trim();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new ValidationException("Category is required");
        }
        this.category = category.trim();
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Quantity must be greater than zero");
        }
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        if (unit == null || unit.trim().isEmpty()) {
            throw new ValidationException("Unit (e.g. kg, quintal, tonne) is required");
        }
        this.unit = unit.trim().toLowerCase();
    }

    public BigDecimal getMinPricePerUnit() {
        return minPricePerUnit;
    }

    public void setMinPricePerUnit(BigDecimal minPricePerUnit) {
        if (minPricePerUnit == null || minPricePerUnit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Minimum price per unit must be greater than zero");
        }
        this.minPricePerUnit = minPricePerUnit;
    }

    public BigDecimal getAgentOfferPerUnit() {
        return agentOfferPerUnit;
    }

    public void setAgentOfferPerUnit(BigDecimal agentOfferPerUnit) {
        if (agentOfferPerUnit != null && agentOfferPerUnit.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Local agent offer price cannot be negative");
        }
        this.agentOfferPerUnit = (agentOfferPerUnit != null) ? agentOfferPerUnit : BigDecimal.ZERO;
    }

    public LotStatus getStatus() {
        return status;
    }

    public void setStatus(LotStatus status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getFarmerName() {
        return farmerName;
    }

    public void setFarmerName(String farmerName) {
        this.farmerName = farmerName;
    }

    public String getFarmerLocation() {
        return farmerLocation;
    }

    public void setFarmerLocation(String farmerLocation) {
        this.farmerLocation = farmerLocation;
    }

    public Integer getActiveAuctionId() {
        return activeAuctionId;
    }

    public void setActiveAuctionId(Integer activeAuctionId) {
        this.activeAuctionId = activeAuctionId;
    }

    public BigDecimal getCurrentHighestBid() {
        return currentHighestBid;
    }

    public void setCurrentHighestBid(BigDecimal currentHighestBid) {
        this.currentHighestBid = currentHighestBid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProduceLot that)) return false;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ProduceLot{" +
                "id=" + id +
                ", cropName='" + cropName + '\'' +
                ", quantity=" + quantity + " " + unit +
                ", minPrice=" + minPricePerUnit +
                ", status=" + status +
                '}';
    }
}

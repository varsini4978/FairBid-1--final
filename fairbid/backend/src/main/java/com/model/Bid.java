package com.model;

import java.sql.Timestamp;

public class Bid {
    private int id;
    private int bidId;
    private int cropId;
    private String buyerName;
    private String buyerEmail;
    private double bidAmount;
    private String status;
    private Timestamp createdAt;
    private String createdAtIso;

    // Display fields for buyer dashboard joins
    private String cropName;
    private Integer quantity;
    private String location;
    private String cropStatus;

    public Bid() {}

    public Bid(int cropId, String buyerEmail, double bidAmount) {
        this.cropId = cropId;
        this.buyerEmail = buyerEmail;
        this.bidAmount = bidAmount;
        this.status = "PENDING";
    }

    public Bid(int id, int cropId, String buyerName, String buyerEmail, double bidAmount, String status, Timestamp createdAt) {
        this.id = id;
        this.bidId = id;
        this.cropId = cropId;
        this.buyerName = buyerName;
        this.buyerEmail = buyerEmail;
        this.bidAmount = bidAmount;
        this.status = status;
        this.createdAt = createdAt;
        if (createdAt != null) {
            this.createdAtIso = createdAt.toInstant().toString();
        }
    }

    public int getId() { return id; }
    public void setId(int id) {
        this.id = id;
        this.bidId = id;
    }

    public int getBidId() { return bidId != 0 ? bidId : id; }
    public void setBidId(int bidId) {
        this.bidId = bidId;
        this.id = bidId;
    }

    public int getCropId() { return cropId; }
    public void setCropId(int cropId) { this.cropId = cropId; }

    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

    public String getBuyerEmail() { return buyerEmail; }
    public void setBuyerEmail(String buyerEmail) { this.buyerEmail = buyerEmail; }

    public double getBidAmount() { return bidAmount; }
    public void setBidAmount(double bidAmount) { this.bidAmount = bidAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
        if (createdAt != null) {
            this.createdAtIso = createdAt.toInstant().toString();
        }
    }

    public String getCreatedAtIso() { return createdAtIso; }
    public void setCreatedAtIso(String createdAtIso) { this.createdAtIso = createdAtIso; }

    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCropStatus() { return cropStatus; }
    public void setCropStatus(String cropStatus) { this.cropStatus = cropStatus; }
}

package com.model;

import java.sql.Timestamp;

public class Notification {
    private int id;
    private String farmerId;
    private int cropId;
    private int bidId;
    private String buyerName;
    private String cropName;
    private double bidAmount;
    private boolean belowMsp;
    private boolean read;
    private Timestamp createdAt;
    private String createdAtIso;

    public Notification() {}

    public Notification(int id, String farmerId, int cropId, int bidId, String buyerName, String cropName, double bidAmount, boolean belowMsp, boolean read, Timestamp createdAt) {
        this.id = id;
        this.farmerId = farmerId;
        this.cropId = cropId;
        this.bidId = bidId;
        this.buyerName = buyerName;
        this.cropName = cropName;
        this.bidAmount = bidAmount;
        this.belowMsp = belowMsp;
        this.read = read;
        this.createdAt = createdAt;
        if (createdAt != null) {
            this.createdAtIso = createdAt.toInstant().toString();
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFarmerId() { return farmerId; }
    public void setFarmerId(String farmerId) { this.farmerId = farmerId; }

    public int getCropId() { return cropId; }
    public void setCropId(int cropId) { this.cropId = cropId; }

    public int getBidId() { return bidId; }
    public void setBidId(int bidId) { this.bidId = bidId; }

    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }

    public double getBidAmount() { return bidAmount; }
    public void setBidAmount(double bidAmount) { this.bidAmount = bidAmount; }

    public boolean isBelowMsp() { return belowMsp; }
    public void setBelowMsp(boolean belowMsp) { this.belowMsp = belowMsp; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
        if (createdAt != null) {
            this.createdAtIso = createdAt.toInstant().toString();
        }
    }

    public String getCreatedAtIso() { return createdAtIso; }
    public void setCreatedAtIso(String createdAtIso) { this.createdAtIso = createdAtIso; }
}

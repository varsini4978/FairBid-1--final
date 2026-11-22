package com.model;

import java.sql.Timestamp;

public class Crop {
    private int id;
    private String farmerId;
    private String cropName;
    private int quantity;
    private double minPrice;
    private String location;
    private String status;
    private Timestamp createdAt;
    private String farmerName;
    private String qualityGrade;
    private String description;
    private Integer acceptedBidId;
    private Double agreedPrice;
    private Timestamp soldAt;

    public Crop() {}

    public Crop(String farmerId, String cropName, int quantity, double minPrice, String location) {
        this.farmerId = farmerId;
        this.cropName = cropName;
        this.quantity = quantity;
        this.minPrice = minPrice;
        this.location = location;
        this.status = "OPEN";
        this.qualityGrade = "Grade A";
    }

    public Crop(int id, String farmerId, String cropName, int quantity, double minPrice, String location,
                String status, Timestamp createdAt, String farmerName, String qualityGrade,
                String description, Integer acceptedBidId, Double agreedPrice, Timestamp soldAt) {
        this.id = id;
        this.farmerId = farmerId;
        this.cropName = cropName;
        this.quantity = quantity;
        this.minPrice = minPrice;
        this.location = location;
        this.status = status;
        this.createdAt = createdAt;
        this.farmerName = farmerName;
        this.qualityGrade = qualityGrade;
        this.description = description;
        this.acceptedBidId = acceptedBidId;
        this.agreedPrice = agreedPrice;
        this.soldAt = soldAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFarmerId() { return farmerId; }
    public void setFarmerId(String farmerId) { this.farmerId = farmerId; }

    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getMinPrice() { return minPrice; }
    public void setMinPrice(double minPrice) { this.minPrice = minPrice; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getFarmerName() { return farmerName; }
    public void setFarmerName(String farmerName) { this.farmerName = farmerName; }

    public String getQualityGrade() { return qualityGrade; }
    public void setQualityGrade(String qualityGrade) { this.qualityGrade = qualityGrade; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getAcceptedBidId() { return acceptedBidId; }
    public void setAcceptedBidId(Integer acceptedBidId) { this.acceptedBidId = acceptedBidId; }

    public Double getAgreedPrice() { return agreedPrice; }
    public void setAgreedPrice(Double agreedPrice) { this.agreedPrice = agreedPrice; }

    public Timestamp getSoldAt() { return soldAt; }
    public void setSoldAt(Timestamp soldAt) { this.soldAt = soldAt; }
}

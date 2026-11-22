package com.model;

public class Farmer {
    private String farmerId, fullName, state, primaryCrop, password;
    private String verificationStatus; // PENDING, DEMO_VERIFIED, GOVT_VERIFIED
    private String verificationType;

    public Farmer(String farmerId, String fullName, String state, String primaryCrop, String password) {
        this(farmerId, fullName, state, primaryCrop, password, "PENDING", "AgriStack");
    }

    public Farmer(String farmerId, String fullName, String state, String primaryCrop, String password, String verificationStatus, String verificationType) {
        this.farmerId = farmerId;
        this.fullName = fullName;
        this.state = state;
        this.primaryCrop = primaryCrop;
        this.password = password;
        this.verificationStatus = (verificationStatus != null && !verificationStatus.isEmpty()) ? verificationStatus : "PENDING";
        this.verificationType = (verificationType != null && !verificationType.isEmpty()) ? verificationType : "AgriStack";
    }

    public String getFarmerId() { return farmerId; }
    public String getFullName() { return fullName; }
    public String getState() { return state; }
    public String getPrimaryCrop() { return primaryCrop; }
    public String getPassword() { return password; }
    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String s) { this.verificationStatus = s; }
    public String getVerificationType() { return verificationType; }
    public void setVerificationType(String t) { this.verificationType = t; }
}

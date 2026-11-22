package com.model;

public class Buyer {
    private String name, email, password, category, docNumber;
    private String verificationStatus; // PENDING, DEMO_VERIFIED, GOVT_VERIFIED
    private String verificationType;

    public Buyer(String name, String email, String password, String category, String docNumber) {
        this(name, email, password, category, docNumber, "PENDING", "Aadhaar e-KYC");
    }

    public Buyer(String name, String email, String password, String category, String docNumber, String verificationStatus, String verificationType) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.category = category;
        this.docNumber = docNumber;
        this.verificationStatus = (verificationStatus != null && !verificationStatus.isEmpty()) ? verificationStatus : "PENDING";
        this.verificationType = (verificationType != null && !verificationType.isEmpty()) ? verificationType : "Aadhaar e-KYC";
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getCategory() { return category; }
    public String getDocNumber() { return docNumber; }
    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String s) { this.verificationStatus = s; }
    public String getVerificationType() { return verificationType; }
    public void setVerificationType(String t) { this.verificationType = t; }
}

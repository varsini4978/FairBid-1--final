package com.fairbid.model;

import com.fairbid.exception.ValidationException;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Abstraction & Encapsulation:
 * Abstract base class for all system users (Farmer, Buyer).
 * Encapsulates common attributes and enforces data integrity in getters/setters.
 */
public abstract class User {
    private int id;
    private String fullName;
    private String email;
    private String passwordHash;
    private String phone;
    private String location;
    private Timestamp createdAt;

    public User() {
    }

    public User(int id, String fullName, String email, String passwordHash, String phone, String location, Timestamp createdAt) {
        setId(id);
        setFullName(fullName);
        setEmail(email);
        setPasswordHash(passwordHash);
        setPhone(phone);
        setLocation(location);
        this.createdAt = createdAt != null ? createdAt : new Timestamp(System.currentTimeMillis());
    }

    // -------------------------------------------------------------------------
    // Abstract Methods (Polymorphism: overridden by subclasses)
    // -------------------------------------------------------------------------
    public abstract Role getRole();

    public abstract String getDashboardPath();

    public abstract String getProfileSummary();

    // -------------------------------------------------------------------------
    // Encapsulated Getters & Setters with Validation
    // -------------------------------------------------------------------------
    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) {
            throw new ValidationException("User ID cannot be negative");
        }
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ValidationException("Full name is required");
        }
        this.fullName = fullName.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ValidationException("A valid email address is required");
        }
        this.email = email.trim().toLowerCase();
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.trim().isEmpty()) {
            throw new ValidationException("Password hash cannot be empty");
        }
        this.passwordHash = passwordHash;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        if (phone != null && !phone.trim().isEmpty()) {
            String digits = phone.replaceAll("[^0-9]", "");
            if (digits.length() < 10 || digits.length() > 15) {
                throw new ValidationException("Phone number must contain between 10 and 15 digits");
            }
            this.phone = digits;
        } else {
            this.phone = "";
        }
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = (location != null) ? location.trim() : "";
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return id == user.id && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", role=" + getRole() +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}

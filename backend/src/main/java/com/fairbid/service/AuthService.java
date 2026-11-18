package com.fairbid.service;

import com.fairbid.dao.UserDAO;
import com.fairbid.exception.AuthenticationException;
import com.fairbid.exception.ResourceNotFoundException;
import com.fairbid.exception.ValidationException;
import com.fairbid.model.Buyer;
import com.fairbid.model.Farmer;
import com.fairbid.model.Role;
import com.fairbid.model.User;
import com.fairbid.util.PasswordUtil;
import com.fairbid.util.ValidationUtil;

import java.sql.Timestamp;

/**
 * Service managing user registration, authentication, and profile updates.
 */
public class AuthService {
    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User authenticate(String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new AuthenticationException("Email and password are required");
        }
        User user = userDAO.findByEmail(email.trim());
        if (user == null || !PasswordUtil.verify(password, user.getPasswordHash())) {
            throw new AuthenticationException("Invalid email or password");
        }
        return user;
    }

    public Farmer registerFarmer(String fullName, String email, String password, String phone,
                                 String location, String farmName, String village, String district) {
        ValidationUtil.validateNotBlank(fullName, "Full name");
        ValidationUtil.validateEmail(email);
        ValidationUtil.validatePasswordStrength(password);
        ValidationUtil.validatePhone(phone);
        ValidationUtil.validateNotBlank(farmName, "Farm name");

        Farmer farmer = new Farmer(
                0,
                fullName.trim(),
                email.trim().toLowerCase(),
                PasswordUtil.hash(password),
                phone != null ? phone.trim() : "",
                location != null ? location.trim() : "",
                new Timestamp(System.currentTimeMillis()),
                farmName.trim(),
                village != null ? village.trim() : "",
                district != null ? district.trim() : ""
        );

        return (Farmer) userDAO.registerFarmer(farmer);
    }

    public Buyer registerBuyer(String fullName, String email, String password, String phone,
                               String location, String businessName, String buyerType) {
        ValidationUtil.validateNotBlank(fullName, "Full name");
        ValidationUtil.validateEmail(email);
        ValidationUtil.validatePasswordStrength(password);
        ValidationUtil.validatePhone(phone);
        ValidationUtil.validateNotBlank(businessName, "Business name");

        Buyer buyer = new Buyer(
                0,
                fullName.trim(),
                email.trim().toLowerCase(),
                PasswordUtil.hash(password),
                phone != null ? phone.trim() : "",
                location != null ? location.trim() : "",
                new Timestamp(System.currentTimeMillis()),
                businessName.trim(),
                buyerType != null && !buyerType.trim().isEmpty() ? buyerType.trim() : "Wholesaler"
        );

        return (Buyer) userDAO.registerBuyer(buyer);
    }

    public User getProfile(int userId) {
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        return user;
    }

    public User updateProfile(int userId, String fullName, String phone, String location,
                              String extra1, String extra2, String extra3) {
        User user = getProfile(userId);
        ValidationUtil.validateNotBlank(fullName, "Full name");
        ValidationUtil.validatePhone(phone);

        user.setFullName(fullName.trim());
        user.setPhone(phone != null ? phone.trim() : "");
        user.setLocation(location != null ? location.trim() : "");

        if (user instanceof Farmer farmer) {
            if (extra1 != null) farmer.setFarmName(extra1);
            if (extra2 != null) farmer.setVillage(extra2);
            if (extra3 != null) farmer.setDistrict(extra3);
        } else if (user instanceof Buyer buyer) {
            if (extra1 != null) buyer.setBusinessName(extra1);
            if (extra2 != null) buyer.setBuyerType(extra2);
        }

        userDAO.updateProfile(user);
        return user;
    }
}

package com.fairbid.util;

import com.fairbid.exception.ValidationException;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Server-side input validation helper.
 * Validates emails, passwords, phone numbers, and financial quantities.
 */
public final class ValidationUtil {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10,15}$");

    private ValidationUtil() {
    }

    public static void validateEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Please provide a valid email address (e.g. farmer@fairbid.com)");
        }
    }

    public static void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters long");
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        if (!hasLetter || !hasDigit) {
            throw new ValidationException("Password must contain at least one letter and one number");
        }
    }

    public static void validatePhone(String phone) {
        if (phone == null) return;
        String clean = phone.replaceAll("[^0-9]", "");
        if (!clean.isEmpty() && !PHONE_PATTERN.matcher(clean).matches()) {
            throw new ValidationException("Phone number must contain between 10 and 15 digits");
        }
    }

    public static void validatePositive(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(fieldName + " must be strictly greater than zero");
        }
    }

    public static void validateNonNegative(BigDecimal value, String fieldName) {
        if (value != null && value.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException(fieldName + " cannot be negative");
        }
    }

    public static void validateNotBlank(String text, String fieldName) {
        if (text == null || text.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be blank");
        }
    }
}

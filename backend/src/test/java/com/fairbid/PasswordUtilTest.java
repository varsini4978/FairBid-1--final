package com.fairbid;

import com.fairbid.util.PasswordUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BCrypt password hashing and verification.
 */
public class PasswordUtilTest {

    @Test
    @DisplayName("Should securely hash and verify Farmer password")
    void testFarmerPasswordHashing() {
        String plain = "Farmer@123";
        String hash = PasswordUtil.hash(plain);

        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$12$"));
        assertTrue(PasswordUtil.verify(plain, hash));
        assertFalse(PasswordUtil.verify("WrongPassword", hash));
        assertFalse(PasswordUtil.verify("", hash));
        assertFalse(PasswordUtil.verify(null, hash));
    }

    @Test
    @DisplayName("Should securely hash and verify Buyer password")
    void testBuyerPasswordHashing() {
        String plain = "Buyer@123";
        String hash = PasswordUtil.hash(plain);

        assertNotNull(hash);
        assertTrue(PasswordUtil.verify(plain, hash));
        assertFalse(PasswordUtil.verify("buyer@123", hash)); // case sensitive
    }

    @Test
    @DisplayName("Should verify precomputed sample hashes from database")
    void testPrecomputedSampleHashes() {
        String sampleFarmerHash = "$2a$12$olUQo7C7Bt9Y7yMQ7KWHeeGFhRmbDEMYSTB6d3tRUOG.LE3Nc8AFi";
        String sampleBuyerHash = "$2a$12$iJg194/J3xOw2wYzWMJ45.1.QfNdPdH4nyHs1ncf./4TCPtEmSxU.";

        assertTrue(PasswordUtil.verify("Farmer@123", sampleFarmerHash), "Precomputed farmer hash must verify");
        assertTrue(PasswordUtil.verify("Buyer@123", sampleBuyerHash), "Precomputed buyer hash must verify");
    }
}

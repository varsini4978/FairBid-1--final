package com.fairbid;

import com.fairbid.model.Buyer;
import com.fairbid.model.Farmer;
import com.fairbid.model.Role;
import com.fairbid.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying OOP Inheritance and Dynamic Polymorphic Dispatch on User hierarchy.
 */
public class UserRoleTest {

    @Test
    @DisplayName("Farmer subclass should override getRole, getDashboardPath and getProfileSummary correctly")
    void testFarmerPolymorphicBehavior() {
        User user = new Farmer(
                1,
                "Ramesh Kumar",
                "farmer1@fairbid.com",
                "$2a$12$olUQo7C7Bt9Y7yMQ7KWHeeGFhRmbDEMYSTB6d3tRUOG.LE3Nc8AFi",
                "9876543210",
                "Thanjavur, Tamil Nadu",
                new Timestamp(System.currentTimeMillis()),
                "Green Valley Organics",
                "Papanasam",
                "Thanjavur"
        );

        // Polymorphic invocations via User reference
        assertEquals(Role.FARMER, user.getRole());
        assertEquals("farmer-dashboard.html", user.getDashboardPath());
        assertTrue(user.getProfileSummary().contains("Green Valley Organics"));
        assertTrue(user.getProfileSummary().contains("Papanasam"));
    }

    @Test
    @DisplayName("Buyer subclass should override getRole, getDashboardPath and getProfileSummary correctly")
    void testBuyerPolymorphicBehavior() {
        User user = new Buyer(
                4,
                "Priya Agro Traders",
                "buyer1@fairbid.com",
                "$2a$12$iJg194/J3xOw2wYzWMJ45.1.QfNdPdH4nyHs1ncf./4TCPtEmSxU.",
                "9123456780",
                "Chennai, Tamil Nadu",
                new Timestamp(System.currentTimeMillis()),
                "Priya Agro Traders Pvt Ltd",
                "Wholesale Distributor"
        );

        // Polymorphic invocations via User reference
        assertEquals(Role.BUYER, user.getRole());
        assertEquals("buyer-dashboard.html", user.getDashboardPath());
        assertTrue(user.getProfileSummary().contains("Priya Agro Traders Pvt Ltd"));
        assertTrue(user.getProfileSummary().contains("Wholesale Distributor"));
    }

    @Test
    @DisplayName("User encapsulation should enforce validation on emails and phone numbers")
    void testUserEncapsulationValidation() {
        Farmer farmer = new Farmer();
        assertThrows(RuntimeException.class, () -> farmer.setEmail("invalid-email"));
        assertThrows(RuntimeException.class, () -> farmer.setFullName("   "));
        assertThrows(RuntimeException.class, () -> farmer.setPhone("123")); // too short
    }
}

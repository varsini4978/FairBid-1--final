package com.fairbid.servlet;

import com.fairbid.exception.ValidationException;
import com.fairbid.model.Buyer;
import com.fairbid.model.Farmer;
import com.fairbid.model.User;
import com.fairbid.service.AuthService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Profile Servlet managing profile viewing and editing for authenticated users.
 */
@WebServlet(name = "ProfileServlet", urlPatterns = {"/api/profile"})
public class ProfileServlet extends BaseServlet {
    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int userId = getAuthenticatedUserId(req);
            User user = authService.getProfile(userId);
            sendSuccess(resp, createProfileMap(user));
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int userId = getAuthenticatedUserId(req);
            @SuppressWarnings("unchecked")
            Map<String, String> body = parseJsonBody(req, Map.class);
            if (body == null) throw new ValidationException("Request body cannot be empty");

            String fullName = body.get("fullName");
            String phone = body.get("phone");
            String location = body.get("location");
            String extra1 = body.get("extra1"); // farmName or businessName
            String extra2 = body.get("extra2"); // village or buyerType
            String extra3 = body.get("extra3"); // district

            User updated = authService.updateProfile(userId, fullName, phone, location, extra1, extra2, extra3);
            // Update session cache
            req.getSession().setAttribute("user", updated);

            sendSuccess(resp, createProfileMap(updated));
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    private Map<String, Object> createProfileMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("fullName", user.getFullName());
        map.put("email", user.getEmail());
        map.put("role", user.getRole().name());
        map.put("phone", user.getPhone());
        map.put("location", user.getLocation());
        map.put("dashboardPath", user.getDashboardPath());
        map.put("profileSummary", user.getProfileSummary());

        if (user instanceof Farmer farmer) {
            map.put("farmName", farmer.getFarmName());
            map.put("village", farmer.getVillage());
            map.put("district", farmer.getDistrict());
        } else if (user instanceof Buyer buyer) {
            map.put("businessName", buyer.getBusinessName());
            map.put("buyerType", buyer.getBuyerType());
        }
        return map;
    }
}

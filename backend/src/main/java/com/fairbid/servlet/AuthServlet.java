package com.fairbid.servlet;

import com.fairbid.exception.AuthenticationException;
import com.fairbid.exception.ValidationException;
import com.fairbid.model.Role;
import com.fairbid.model.User;
import com.fairbid.service.AuthService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Servlet managing registration, login, logout, and current session inspection.
 * Demonstrates Polymorphism by returning user.getDashboardPath() without inspecting role type.
 */
@WebServlet(name = "AuthServlet", urlPatterns = {"/api/auth/*"})
public class AuthServlet extends BaseServlet {
    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if ("/me".equals(pathInfo)) {
            User user = getAuthenticatedUser(req);
            if (user == null) {
                sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Not authenticated", null);
                return;
            }
            sendSuccess(resp, createUserResponse(user));
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown auth endpoint", null);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        try {
            if ("/login".equals(pathInfo)) {
                handleLogin(req, resp);
            } else if ("/register".equals(pathInfo)) {
                handleRegister(req, resp);
            } else if ("/logout".equals(pathInfo)) {
                handleLogout(req, resp);
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown auth endpoint", null);
            }
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        @SuppressWarnings("unchecked")
        Map<String, String> body = parseJsonBody(req, Map.class);
        if (body == null) {
            throw new ValidationException("Request body cannot be empty");
        }
        String email = body.get("email");
        String password = body.get("password");

        User user = authService.authenticate(email, password);

        // Security: Prevent Session Fixation by acquiring new session ID
        HttpSession oldSession = req.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        HttpSession newSession = req.getSession(true);
        newSession.setAttribute("user", user);
        newSession.setMaxInactiveInterval(3600); // 1 hour timeout

        Map<String, Object> respData = createUserResponse(user);
        sendSuccess(resp, respData);
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        @SuppressWarnings("unchecked")
        Map<String, String> body = parseJsonBody(req, Map.class);
        if (body == null) {
            throw new ValidationException("Request body cannot be empty");
        }

        String roleStr = body.get("role");
        if (roleStr == null) {
            throw new ValidationException("Role (FARMER or BUYER) is required");
        }

        Role role;
        try {
            role = Role.valueOf(roleStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Role must be either FARMER or BUYER");
        }

        String fullName = body.get("fullName");
        String email = body.get("email");
        String password = body.get("password");
        String phone = body.get("phone");
        String location = body.get("location");

        User registered;
        if (role == Role.FARMER) {
            String farmName = body.get("farmName");
            String village = body.get("village");
            String district = body.get("district");
            registered = authService.registerFarmer(fullName, email, password, phone, location, farmName, village, district);
        } else {
            String businessName = body.get("businessName");
            String buyerType = body.get("buyerType");
            registered = authService.registerBuyer(fullName, email, password, phone, location, businessName, buyerType);
        }

        sendCreated(resp, "Registration successful. You can now log in.", createUserResponse(registered));
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        sendSuccess(resp, Map.of("loggedOut", true));
    }

    /**
     * Polymorphic User Payload:
     * Calls user.getDashboardPath() and user.getProfileSummary() on the base reference.
     * Never leaks password hashes.
     */
    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("fullName", user.getFullName());
        map.put("email", user.getEmail());
        map.put("role", user.getRole().name());
        map.put("phone", user.getPhone());
        map.put("location", user.getLocation());
        // Dynamic Method Dispatch:
        map.put("dashboardPath", user.getDashboardPath());
        map.put("profileSummary", user.getProfileSummary());
        return map;
    }
}

package com.controller;

import com.dao.BuyerDAO;
import com.dao.FarmerDAO;
import com.model.Buyer;
import com.model.Farmer;
import com.util.CorsUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        CorsUtil.setCorsHeaders(req, resp);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = req.getReader().readLine()) != null) {
                sb.append(line);
            }
            String requestBody = sb.toString();
            System.out.println("AuthServlet received body: " + requestBody);
            JsonObject json = JsonParser.parseString(requestBody).getAsJsonObject();
            String action = req.getPathInfo();

            if ("/farmer-register".equals(action)) {
                String farmerId = json.has("farmerId") ? json.get("farmerId").getAsString().trim() : "";
                String fullName = json.has("fullName") ? json.get("fullName").getAsString().trim() : "";
                String state = json.has("state") ? json.get("state").getAsString().trim() : "";
                String primaryCrop = json.has("primaryCrop") ? json.get("primaryCrop").getAsString().trim() : "";
                String password = json.has("password") ? json.get("password").getAsString().trim() : "";

                if (farmerId.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                    CorsUtil.writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "All fields are required.");
                    return;
                }

                // Check if already registered
                if (FarmerDAO.getFarmerById(farmerId) != null) {
                    CorsUtil.writeJsonError(resp, HttpServletResponse.SC_CONFLICT, "This Farmer ID or phone number is already registered.");
                    return;
                }

                boolean success = FarmerDAO.register(new Farmer(farmerId, fullName, state, primaryCrop, password));
                if (success) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("{\"success\": true, \"message\": \"Farmer registered successfully!\"}");
                } else {
                    CorsUtil.writeJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Registration failed in database.");
                }

            } else if ("/farmer-login".equals(action)) {
                String farmerId = json.has("farmerId") ? json.get("farmerId").getAsString().trim() : "";
                String password = json.has("password") ? json.get("password").getAsString().trim() : "";

                Farmer farmer = FarmerDAO.getFarmer(farmerId, password);
                if (farmer != null) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    JsonObject out = new JsonObject();
                    out.addProperty("success", true);
                    out.add("farmer", gson.toJsonTree(farmer));
                    resp.getWriter().write(gson.toJson(out));
                } else {
                    CorsUtil.writeJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Invalid Farmer ID or password.");
                }

            } else if ("/buyer-register".equals(action)) {
                String name = json.has("name") ? json.get("name").getAsString().trim() : "";
                String email = json.has("email") ? json.get("email").getAsString().trim() : "";
                String password = json.has("password") ? json.get("password").getAsString().trim() : "";
                String category = json.has("category") ? json.get("category").getAsString().trim() : "";
                String docNumber = json.has("docNumber") ? json.get("docNumber").getAsString().trim() : "";

                if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                    CorsUtil.writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Name, email, and password are required.");
                    return;
                }

                if (BuyerDAO.getBuyerByEmail(email) != null) {
                    CorsUtil.writeJsonError(resp, HttpServletResponse.SC_CONFLICT, "This email address is already registered.");
                    return;
                }

                boolean success = BuyerDAO.register(new Buyer(name, email, password, category, docNumber));
                if (success) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("{\"success\": true, \"message\": \"Buyer registered successfully!\"}");
                } else {
                    CorsUtil.writeJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Registration failed in database.");
                }

            } else if ("/buyer-login".equals(action)) {
                String email = json.has("email") ? json.get("email").getAsString().trim() : "";
                String password = json.has("password") ? json.get("password").getAsString().trim() : "";

                Buyer buyer = BuyerDAO.getBuyer(email, password);
                if (buyer != null) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    JsonObject out = new JsonObject();
                    out.addProperty("success", true);
                    out.add("buyer", gson.toJsonTree(buyer));
                    resp.getWriter().write(gson.toJson(out));
                } else {
                    CorsUtil.writeJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Invalid email or password.");
                }

            } else {
                CorsUtil.writeJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Invalid authentication action path.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            CorsUtil.writeJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        CorsUtil.handleOptions(req, resp);
    }
}

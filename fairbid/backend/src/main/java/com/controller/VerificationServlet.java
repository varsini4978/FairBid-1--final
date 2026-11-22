package com.controller;

import com.dao.BuyerDAO;
import com.dao.FarmerDAO;
import com.dao.VerificationDAO;
import com.model.Buyer;
import com.model.Farmer;
import com.util.CorsUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;

@WebServlet({"/api/verification", "/api/verification/*"})
public class VerificationServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        CorsUtil.setCorsHeaders(req, resp);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            String email = req.getParameter("email");
            String farmerId = req.getParameter("farmerId");

            JsonObject out = new JsonObject();
            out.addProperty("success", true);

            if (email != null && !email.trim().isEmpty()) {
                Buyer b = BuyerDAO.getBuyerByEmail(email.trim());
                if (b != null) {
                    out.addProperty("role", "buyer");
                    out.addProperty("verificationStatus", b.getVerificationStatus());
                    out.addProperty("verificationType", b.getVerificationType());
                } else {
                    CorsUtil.writeJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Buyer not found.");
                    return;
                }
            } else if (farmerId != null && !farmerId.trim().isEmpty()) {
                Farmer f = FarmerDAO.getFarmerById(farmerId.trim());
                if (f != null) {
                    out.addProperty("role", "farmer");
                    out.addProperty("verificationStatus", f.getVerificationStatus());
                    out.addProperty("verificationType", f.getVerificationType());
                } else {
                    CorsUtil.writeJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Farmer not found.");
                    return;
                }
            } else {
                CorsUtil.writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Parameter email or farmerId required.");
                return;
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(gson.toJson(out));
        } catch (Exception e) {
            e.printStackTrace();
            CorsUtil.writeJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        CorsUtil.setCorsHeaders(req, resp);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            StringBuilder sb = new StringBuilder();
            String line;
            try (BufferedReader r = req.getReader()) {
                while ((line = r.readLine()) != null) {
                    sb.append(line);
                }
            }

            JsonObject json = JsonParser.parseString(sb.toString()).getAsJsonObject();
            String path = req.getPathInfo();
            if (path == null) path = "";

            if (path.contains("verify-buyer")) {
                String email = json.has("email") ? json.get("email").getAsString().trim() : "";
                String aadhaarLast4 = json.has("aadhaarLast4") ? json.get("aadhaarLast4").getAsString().trim() : "";
                boolean consent = json.has("consent") && json.get("consent").getAsBoolean();
                String mode = json.has("mode") ? json.get("mode").getAsString().trim().toLowerCase() : "demo";
                String otp = json.has("otp") ? json.get("otp").getAsString().trim() : "";

                if (email.isEmpty()) {
                    CorsUtil.writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Buyer email is required.");
                    return;
                }
                if (!consent) {
                    CorsUtil.writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "User explicit consent is legally required before initiating verification.");
                    return;
                }
                if (aadhaarLast4.length() != 4 || !aadhaarLast4.matches("\\d{4}")) {
                    CorsUtil.writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Please provide the last 4 digits of Aadhaar.");
                    return;
                }

                if ("govt".equals(mode)) {
                    // Check if official UIDAI AUA/KUA environment credentials exist
                    String uidaiApiKey = System.getenv("UIDAI_AUTH_KEY");
                    if (uidaiApiKey == null || uidaiApiKey.isEmpty()) {
                        JsonObject outGovt = new JsonObject();
                        outGovt.addProperty("success", false);
                        outGovt.addProperty("status", "PENDING");
                        outGovt.addProperty("requiresOfficialCredentials", true);
                        outGovt.addProperty("message", "Official UIDAI e-KYC gateway requires authorized AUA/KUA credentials (compliance standard). Please select Demo Verification Mode for testing.");
                        resp.setStatus(HttpServletResponse.SC_OK);
                        resp.getWriter().write(gson.toJson(outGovt));
                        return;
                    }
                }

                // In Demo Mode or Authorized provider flow:
                // Verify OTP (default demo OTP: 123456 or any 6-digit number in demo mode)
                if (otp.isEmpty() || otp.length() < 4) {
                    CorsUtil.writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Please enter the 6-digit OTP sent to registered mobile.");
                    return;
                }

                String newStatus = "DEMO_VERIFIED";
                String newType = "Aadhaar e-KYC (Demo Verified)";
                boolean updated = VerificationDAO.updateBuyerVerification(email, newStatus, newType);

                JsonObject out = new JsonObject();
                out.addProperty("success", updated);
                out.addProperty("status", newStatus);
                out.addProperty("verificationType", newType);
                out.addProperty("maskedAadhaar", "XXXX-XXXX-" + aadhaarLast4);
                out.addProperty("message", "Buyer identity verified successfully in Demo Mode (Mock UIDAI e-KYC).");
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(gson.toJson(out));

            } else if (path.contains("verify-farmer")) {
                String farmerId = json.has("farmerId") ? json.get("farmerId").getAsString().trim() : "";
                String govtFarmerId = json.has("govtFarmerId") ? json.get("govtFarmerId").getAsString().trim() : "";
                boolean consent = json.has("consent") && json.get("consent").getAsBoolean();
                String mode = json.has("mode") ? json.get("mode").getAsString().trim().toLowerCase() : "demo";

                if (farmerId.isEmpty()) {
                    CorsUtil.writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Farmer ID is required.");
                    return;
                }
                if (!consent) {
                    CorsUtil.writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Farmer consent is required to verify against Government Farmer Registry.");
                    return;
                }
                if (govtFarmerId.length() < 4) {
                    CorsUtil.writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Please enter a valid Government Farmer ID (e.g. AgriStack / State Registry ID).");
                    return;
                }

                if ("govt".equals(mode)) {
                    String agriStackKey = System.getenv("AGRISTACK_API_KEY");
                    if (agriStackKey == null || agriStackKey.isEmpty()) {
                        JsonObject outGovt = new JsonObject();
                        outGovt.addProperty("success", false);
                        outGovt.addProperty("status", "PENDING");
                        outGovt.addProperty("requiresOfficialCredentials", true);
                        outGovt.addProperty("message", "Official AgriStack Government Farmer Registry gateway requires registered government API credentials. Please select Demo Verification Mode for testing.");
                        resp.setStatus(HttpServletResponse.SC_OK);
                        resp.getWriter().write(gson.toJson(outGovt));
                        return;
                    }
                }

                String newStatus = "DEMO_VERIFIED";
                String newType = "AgriStack Registry (Demo Verified)";
                boolean updated = VerificationDAO.updateFarmerVerification(farmerId, newStatus, newType);

                JsonObject out = new JsonObject();
                out.addProperty("success", updated);
                out.addProperty("status", newStatus);
                out.addProperty("verificationType", newType);
                out.addProperty("govtFarmerId", govtFarmerId);
                out.addProperty("message", "Farmer ID verified successfully in Demo Mode (Mock AgriStack Registry).");
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(gson.toJson(out));

            } else {
                CorsUtil.writeJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown verification action path.");
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

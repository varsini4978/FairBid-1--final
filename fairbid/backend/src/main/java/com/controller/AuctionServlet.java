package com.controller;

import com.dao.AuctionDAO;
import com.dao.CropDAO;
import com.model.Bid;
import com.model.Crop;
import com.util.CorsUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet({"/api/bids", "/api/bids/*"})
public class AuctionServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        CorsUtil.setCorsHeaders(req, resp);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            String cropIdParam = req.getParameter("cropId");
            if (cropIdParam == null) cropIdParam = req.getParameter("id");

            if (cropIdParam != null && !cropIdParam.trim().isEmpty()) {
                int cropId = Integer.parseInt(cropIdParam.trim());
                List<Bid> bids = AuctionDAO.getBidsByCrop(cropId);
                Crop crop = CropDAO.getCropById(cropId);

                double highestBid = 0.0;
                for (Bid b : bids) {
                    if (b.getBidAmount() > highestBid) {
                        highestBid = b.getBidAmount();
                    }
                }

                resp.setStatus(HttpServletResponse.SC_OK);
                JsonObject out = new JsonObject();
                out.addProperty("success", true);
                out.add("crop", gson.toJsonTree(crop));
                out.add("bids", gson.toJsonTree(bids));
                out.addProperty("highestBid", highestBid);
                out.addProperty("bidCount", bids.size());
                resp.getWriter().write(gson.toJson(out));
                return;
            }

            String buyerEmail = req.getParameter("buyerEmail");
            if (buyerEmail != null && !buyerEmail.trim().isEmpty()) {
                List<Bid> bids = AuctionDAO.getBidsByBuyer(buyerEmail.trim());
                resp.setStatus(HttpServletResponse.SC_OK);
                JsonObject out = new JsonObject();
                out.addProperty("success", true);
                out.add("bids", gson.toJsonTree(bids));
                resp.getWriter().write(gson.toJson(out));
                return;
            }

            CorsUtil.writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Parameter cropId or buyerEmail is required.");

        } catch (NumberFormatException e) {
            CorsUtil.writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid cropId format.");
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
            try (java.io.BufferedReader r = req.getReader()) {
                while ((line = r.readLine()) != null) {
                    sb.append(line);
                }
            }
            JsonObject json = JsonParser.parseString(sb.toString()).getAsJsonObject();
            String path = req.getPathInfo();
            String action = json.has("action") ? json.get("action").getAsString() : null;

            // Handle Revise MSP
            if ("/revise-msp".equals(path) || "reviseMsp".equalsIgnoreCase(action)) {
                int cropId = json.get("cropId").getAsInt();
                double newMsp = json.get("newMsp").getAsDouble();
                String farmerId = json.has("farmerId") ? json.get("farmerId").getAsString().trim() : null;

                boolean updated = AuctionDAO.reviseMsp(cropId, newMsp, farmerId);
                JsonObject outRev = new JsonObject();
                outRev.addProperty("success", updated);
                outRev.addProperty("message", updated ? "MSP updated successfully" : "Failed to update MSP");
                resp.setStatus(updated ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(gson.toJson(outRev));
                return;
            }

            // Handle Accept Anyway
            if ("/accept-anyway".equals(path) || "acceptAnyway".equalsIgnoreCase(action)) {
                int cropId = json.get("cropId").getAsInt();
                int bidId = json.get("bidId").getAsInt();
                String farmerId = json.has("farmerId") ? json.get("farmerId").getAsString().trim() : null;

                AuctionDAO.BidResult result = AuctionDAO.acceptBidRegardlessOfMsp(cropId, bidId, farmerId);
                JsonObject out = new JsonObject();
                out.addProperty("success", result.success);
                out.addProperty("message", result.message);
                resp.setStatus(result.success ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(gson.toJson(out));
                return;
            }

            if ("/accept".equals(path) || "accept".equalsIgnoreCase(action)) {
                int cropId = json.get("cropId").getAsInt();
                int bidId = json.get("bidId").getAsInt();
                String farmerId = json.has("farmerId") ? json.get("farmerId").getAsString().trim() : null;

                AuctionDAO.BidResult result = AuctionDAO.acceptBid(cropId, bidId, farmerId);
                if (result.success) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
                JsonObject out = new JsonObject();
                out.addProperty("success", result.success);
                out.addProperty("message", result.message);
                resp.getWriter().write(gson.toJson(out));
                return;
            }

            // Handle Reject Bid
            if ("/reject".equals(path) || "reject".equalsIgnoreCase(action)) {
                int cropId = json.get("cropId").getAsInt();
                int bidId = json.get("bidId").getAsInt();
                String farmerId = json.has("farmerId") ? json.get("farmerId").getAsString().trim() : null;

                AuctionDAO.BidResult result = AuctionDAO.rejectBid(cropId, bidId, farmerId);
                if (result.success) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
                JsonObject out = new JsonObject();
                out.addProperty("success", result.success);
                out.addProperty("message", result.message);
                resp.getWriter().write(gson.toJson(out));
                return;
            }

            // Handle Standard Place Bid
            int cropId = json.get("cropId").getAsInt();
            String buyerEmail = json.has("buyerEmail") ? json.get("buyerEmail").getAsString().trim() : "";
            double bidAmount = json.get("bidAmount").getAsDouble();
            String buyerName = json.has("buyerName") ? json.get("buyerName").getAsString().trim() : null;

            if (buyerEmail.isEmpty() || bidAmount <= 0) {
                CorsUtil.writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Valid buyerEmail and positive bidAmount are required.");
                return;
            }

            AuctionDAO.BidResult result = AuctionDAO.placeBidWithValidation(cropId, buyerEmail, buyerName, bidAmount);
            if (result.success) {
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            JsonObject out = new JsonObject();
            out.addProperty("success", result.success);
            out.addProperty("message", result.message);
            out.addProperty("bidId", result.bidId);
            out.addProperty("belowMsp", AuctionDAO.isBelowMsp(cropId, bidAmount));
            resp.getWriter().write(gson.toJson(out));

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

package com.fairbid.servlet;

import com.fairbid.exception.ValidationException;
import com.fairbid.interfaces.Biddable;
import com.fairbid.model.*;
import com.fairbid.service.AuctionService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Auction Servlet managing active auction rooms, bid placement, early closings, and settled results.
 * BidServlet's wildcard annotation is non-standard; bid POST is handled here via /api/auctions/{id}/bids.
 * Multiple Interface Usage: calls engine via Biddable reference for bid placement.
 */
@WebServlet(name = "AuctionServlet", urlPatterns = {"/api/auctions/*"})
public class AuctionServlet extends BaseServlet {
    private final AuctionService auctionService = new AuctionService();
    // Interface reference for bid placement (demonstrates Biddable usage from AuctionServlet)
    private final Biddable biddableEngine = auctionService.getAuctionManager();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null || "/".equals(pathInfo)) {
                String category = req.getParameter("category");
                String search = req.getParameter("search");
                List<Auction> open = auctionService.getOpenAuctions(category, search);
                sendSuccess(resp, open);
            } else {
                String[] parts = pathInfo.split("/");
                if (parts.length >= 2) {
                    int auctionId = Integer.parseInt(parts[1]);
                    if (parts.length == 2) {
                        handleGetAuctionDetails(req, resp, auctionId);
                    } else if (parts.length == 3 && "bids".equals(parts[2])) {
                        handleGetBids(req, resp, auctionId);
                    } else if (parts.length == 3 && "result".equals(parts[2])) {
                        AuctionResult result = auctionService.getAuctionResult(auctionId);
                        sendSuccess(resp, result);
                    } else {
                        sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Resource not found", null);
                    }
                } else {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Invalid endpoint", null);
                }
            }
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        try {
            String[] parts = pathInfo != null ? pathInfo.split("/") : new String[0];
            if (parts.length == 3 && "close".equals(parts[2])) {
                int auctionId = Integer.parseInt(parts[1]);
                int farmerId = getAuthenticatedUserId(req);
                AuctionResult result = auctionService.getAuctionManager().closeAuction(auctionId, farmerId);
                sendSuccess(resp, result);
            } else if (parts.length == 3 && "bids".equals(parts[2])) {
                // Bid placement: POST /api/auctions/{id}/bids
                // (BidServlet's wildcard URL is non-standard so we handle it here)
                int auctionId = Integer.parseInt(parts[1]);
                handlePlaceBid(req, resp, auctionId);
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Invalid endpoint", null);
            }
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    /**
     * Place a bid — buyer ID comes from session, never from client.
     * Uses Biddable interface reference for polymorphic dispatch.
     */
    private void handlePlaceBid(HttpServletRequest req, HttpServletResponse resp, int auctionId) throws IOException {
        int buyerId = getAuthenticatedUserId(req);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = parseJsonBody(req, Map.class);
        if (body == null || body.get("amount") == null) {
            throw new ValidationException("Bid amount is required");
        }
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        // Interface method dispatch (polymorphism)
        Bid placed = biddableEngine.placeBid(auctionId, buyerId, amount);
        sendCreated(resp, "Bid placed successfully! You are currently the highest bidder.", placed);
    }

    private void handleGetAuctionDetails(HttpServletRequest req, HttpServletResponse resp, int auctionId) throws IOException {
        Auction auction = auctionService.getAuctionManager().getAuctionDetails(auctionId);
        if (auction == null) {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Auction not found", null);
            return;
        }

        User currentUser = getAuthenticatedUser(req);
        boolean isOwnerFarmer = (currentUser instanceof Farmer) &&
                (auction.getProduceLot() != null && auction.getProduceLot().getFarmerId() == currentUser.getId());

        // Construct masked response if not owning farmer
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("id", auction.getId());
        responseData.put("lotId", auction.getLotId());
        responseData.put("roundNumber", auction.getRoundNumber());
        responseData.put("minPriceSnapshot", auction.getMinPriceSnapshot());
        responseData.put("startTime", auction.getStartTime());
        responseData.put("endTime", auction.getEndTime());
        responseData.put("status", auction.getStatus().name());
        responseData.put("outcome", auction.getOutcome() != null ? auction.getOutcome().name() : null);
        responseData.put("timeRemainingSeconds", auction.getTimeRemainingSeconds());
        responseData.put("produceLot", auction.getProduceLot());

        List<Map<String, Object>> sanitizedBids = new ArrayList<>();
        for (Bid b : auction.getBids()) {
            Map<String, Object> bm = new HashMap<>();
            bm.put("id", b.getId());
            bm.put("amountPerUnit", b.getAmountPerUnit());
            bm.put("placedAt", b.getPlacedAt());
            bm.put("status", b.getStatus().name());

            if (isOwnerFarmer || (currentUser != null && currentUser.getId() == b.getBuyerId())) {
                bm.put("buyerName", b.getBuyerName());
                bm.put("buyerBusiness", b.getBuyerBusiness());
                bm.put("isMyBid", currentUser != null && currentUser.getId() == b.getBuyerId());
            } else {
                bm.put("buyerName", b.getMaskedBuyerName());
                bm.put("isMyBid", false);
            }
            sanitizedBids.add(bm);
        }
        responseData.put("bids", sanitizedBids);
        responseData.put("bidCount", sanitizedBids.size());
        responseData.put("highestBid", auction.getHighestBid());

        sendSuccess(resp, responseData);
    }

    private void handleGetBids(HttpServletRequest req, HttpServletResponse resp, int auctionId) throws IOException {
        Auction auction = auctionService.getAuctionManager().getAuctionDetails(auctionId);
        if (auction == null) {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Auction not found", null);
            return;
        }

        User currentUser = getAuthenticatedUser(req);
        boolean isOwnerFarmer = (currentUser instanceof Farmer) &&
                (auction.getProduceLot() != null && auction.getProduceLot().getFarmerId() == currentUser.getId());

        List<Map<String, Object>> sanitizedBids = new ArrayList<>();
        for (Bid b : auction.getBids()) {
            Map<String, Object> bm = new HashMap<>();
            bm.put("id", b.getId());
            bm.put("amountPerUnit", b.getAmountPerUnit());
            bm.put("placedAt", b.getPlacedAt());
            bm.put("status", b.getStatus().name());

            if (isOwnerFarmer || (currentUser != null && currentUser.getId() == b.getBuyerId())) {
                bm.put("buyerName", b.getBuyerName());
                bm.put("buyerBusiness", b.getBuyerBusiness());
                bm.put("isMyBid", currentUser != null && currentUser.getId() == b.getBuyerId());
            } else {
                bm.put("buyerName", b.getMaskedBuyerName());
                bm.put("isMyBid", false);
            }
            sanitizedBids.add(bm);
        }

        sendSuccess(resp, sanitizedBids);
    }
}

package com.fairbid.servlet;

import com.fairbid.exception.UnauthorizedActionException;
import com.fairbid.exception.ValidationException;
import com.fairbid.interfaces.Sellable;
import com.fairbid.model.*;
import com.fairbid.service.AuctionManager;
import com.fairbid.service.AuctionService;
import com.fairbid.service.ComparisonService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Produce Servlet managing agricultural lot listings, price revisions,
 * auction start/reopening, comparisons, and farmer agent offer decisions.
 * Uses Sellable interface for producer domain operations.
 */
@WebServlet(name = "ProduceServlet", urlPatterns = {"/api/produce/*"})
public class ProduceServlet extends BaseServlet {
    private final AuctionService auctionService = new AuctionService();
    private final ComparisonService comparisonService = new ComparisonService();
    // Multiple Interface Usage: Sellable reference
    private final Sellable sellableEngine = auctionService.getAuctionManager();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null || "/".equals(pathInfo)) {
                // Browse all lots
                String category = req.getParameter("category");
                String search = req.getParameter("search");
                List<ProduceLot> lots = auctionService.browseProduce(category, search);
                sendSuccess(resp, lots);
            } else if ("/mine".equals(pathInfo)) {
                // Farmer's lots
                int farmerId = getAuthenticatedUserId(req);
                List<ProduceLot> lots = auctionService.getFarmerLots(farmerId);
                sendSuccess(resp, lots);
            } else {
                String[] parts = pathInfo.split("/");
                if (parts.length >= 2) {
                    int lotId = Integer.parseInt(parts[1]);
                    if (parts.length == 2) {
                        ProduceLot lot = auctionService.getProduceLot(lotId);
                        sendSuccess(resp, lot);
                    } else if (parts.length == 3 && "comparison".equals(parts[2])) {
                        handleComparison(req, resp, lotId);
                    } else if (parts.length == 3 && "rounds".equals(parts[2])) {
                        List<Auction> rounds = auctionService.getLotRounds(lotId);
                        sendSuccess(resp, rounds);
                    } else if (parts.length == 3 && "agent-offer".equals(parts[2])) {
                        AgentOffer offer = auctionService.getAgentOffer(lotId);
                        if (offer == null) {
                            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "No agent offer found for lot ID: " + lotId, null);
                        } else {
                            sendSuccess(resp, offer);
                        }
                    } else if (parts.length == 3 && "result".equals(parts[2])) {
                        AuctionResult result = auctionService.getLatestResultByLotId(lotId);
                        if (result == null) {
                            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "No settled result found for lot ID: " + lotId, null);
                        } else {
                            sendSuccess(resp, result);
                        }
                    } else {
                        sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Resource not found", null);
                    }
                } else {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Resource not found", null);
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
            int farmerId = getAuthenticatedUserId(req);
            if (pathInfo == null || "/".equals(pathInfo)) {
                handleAddProduce(req, resp, farmerId);
            } else {
                String[] parts = pathInfo.split("/");
                if (parts.length >= 3) {
                    int lotId = Integer.parseInt(parts[1]);
                    String action = parts[2];
                    switch (action) {
                        case "start-auction":
                            handleStartAuction(req, resp, lotId, farmerId);
                            break;
                        case "reopen":
                            handleReopenAuction(req, resp, lotId, farmerId);
                            break;
                        case "revise-price":
                            handleRevisePrice(req, resp, lotId, farmerId);
                            break;
                        case "agent-decision":
                            handleAgentDecision(req, resp, lotId, farmerId);
                            break;
                        default:
                            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown action: " + action, null);
                            break;
                    }
                } else {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Invalid endpoint", null);
                }
            }
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    private void handleAddProduce(HttpServletRequest req, HttpServletResponse resp, int farmerId) throws IOException {
        @SuppressWarnings("unchecked")
        Map<String, Object> body = parseJsonBody(req, Map.class);
        if (body == null) throw new ValidationException("Request body cannot be empty");

        String cropName = (String) body.get("cropName");
        String category = (String) body.get("category");
        String unit = (String) body.get("unit");

        BigDecimal quantity = new BigDecimal(body.get("quantity").toString());
        BigDecimal minPrice = new BigDecimal(body.get("minPricePerUnit").toString());
        BigDecimal agentOffer = body.get("agentOfferPerUnit") != null ?
                new BigDecimal(body.get("agentOfferPerUnit").toString()) : BigDecimal.ZERO;

        ProduceLot lot = new ProduceLot();
        lot.setFarmerId(farmerId);
        lot.setCropName(cropName);
        lot.setCategory(category);
        lot.setQuantity(quantity);
        lot.setUnit(unit);
        lot.setMinPricePerUnit(minPrice);
        lot.setAgentOfferPerUnit(agentOffer);
        lot.setStatus(LotStatus.AVAILABLE);

        // Call via Sellable interface
        ProduceLot created = sellableEngine.listProduce(lot);

        // Optional immediate auction start if closing time provided
        if (body.get("closingTime") != null && !body.get("closingTime").toString().trim().isEmpty()) {
            LocalDateTime closingTime = LocalDateTime.parse(body.get("closingTime").toString());
            auctionService.getAuctionManager().startAuction(created.getId(), farmerId, closingTime);
            created.setStatus(LotStatus.IN_AUCTION);
        }

        sendCreated(resp, "Produce lot listed successfully", created);
    }

    private void handleStartAuction(HttpServletRequest req, HttpServletResponse resp, int lotId, int farmerId) throws IOException {
        @SuppressWarnings("unchecked")
        Map<String, String> body = parseJsonBody(req, Map.class);
        if (body == null || body.get("endTime") == null) {
            throw new ValidationException("Closing time (endTime) is required to start an auction");
        }
        LocalDateTime endTime = LocalDateTime.parse(body.get("endTime"));
        Auction auction = auctionService.getAuctionManager().startAuction(lotId, farmerId, endTime);
        sendCreated(resp, "Auction started successfully", auction);
    }

    private void handleReopenAuction(HttpServletRequest req, HttpServletResponse resp, int lotId, int farmerId) throws IOException {
        @SuppressWarnings("unchecked")
        Map<String, String> body = parseJsonBody(req, Map.class);
        LocalDateTime newEndTime;
        if (body != null && body.get("newEndTime") != null && !body.get("newEndTime").trim().isEmpty()) {
            newEndTime = LocalDateTime.parse(body.get("newEndTime").trim());
        } else {
            // Default: 24 hours from now
            newEndTime = LocalDateTime.now().plusHours(24);
        }
        // Sellable interface call
        Auction round = sellableEngine.reopenAuction(lotId, farmerId, newEndTime);
        sendCreated(resp, "New auction round opened successfully", round);
    }

    private void handleRevisePrice(HttpServletRequest req, HttpServletResponse resp, int lotId, int farmerId) throws IOException {
        @SuppressWarnings("unchecked")
        Map<String, Object> body = parseJsonBody(req, Map.class);
        Object priceVal = body != null ? (body.get("newMinPrice") != null ? body.get("newMinPrice") : body.get("minimumPrice")) : null;
        if (priceVal == null) {
            throw new ValidationException("New minimum price (newMinPrice) is required");
        }
        BigDecimal newMinPrice = new BigDecimal(priceVal.toString());
        // Sellable interface call
        sellableEngine.reviseMinimumPrice(lotId, farmerId, newMinPrice);

        // If new closing time provided (or openNow flag set), automatically start new round with revised price
        LocalDateTime newEndTime = null;
        if (body.get("newEndTime") != null && !body.get("newEndTime").toString().trim().isEmpty()) {
            newEndTime = LocalDateTime.parse(body.get("newEndTime").toString().trim());
        } else {
            newEndTime = LocalDateTime.now().plusHours(24);
        }
        Auction round = sellableEngine.reopenAuction(lotId, farmerId, newEndTime);
        sendSuccess(resp, Map.of("revisedPrice", newMinPrice, "newRound", round, "message", "Minimum price revised and new auction round launched!"));
    }

    private void handleAgentDecision(HttpServletRequest req, HttpServletResponse resp, int lotId, int farmerId) throws IOException {
        @SuppressWarnings("unchecked")
        Map<String, Object> body = parseJsonBody(req, Map.class);
        if (body == null || body.get("decision") == null) {
            throw new ValidationException("Decision (ACCEPTED or REJECTED) is required");
        }
        AgentDecision decision = AgentDecision.valueOf(body.get("decision").toString().toUpperCase());
        BigDecimal transportCost = body.get("transportCost") != null ? new BigDecimal(body.get("transportCost").toString()) : BigDecimal.ZERO;
        BigDecimal otherExpenses = body.get("otherExpenses") != null ? new BigDecimal(body.get("otherExpenses").toString()) : BigDecimal.ZERO;

        // Sellable interface call
        sellableEngine.decideOnAgentOffer(lotId, farmerId, decision, transportCost, otherExpenses);
        sendSuccess(resp, Map.of("decision", decision.name(), "lotId", lotId, "message", "Decision recorded successfully"));
    }

    private void handleComparison(HttpServletRequest req, HttpServletResponse resp, int lotId) throws IOException {
        BigDecimal pTransport = parseBigDecimalParam(req.getParameter("platformTransport"));
        BigDecimal pOther = parseBigDecimalParam(req.getParameter("platformOther"));
        BigDecimal aTransport = parseBigDecimalParam(req.getParameter("agentTransport"));
        BigDecimal aOther = parseBigDecimalParam(req.getParameter("agentOther"));

        PriceComparison pc = comparisonService.compare(lotId, pTransport, pOther, aTransport, aOther);
        sendSuccess(resp, pc);
    }

    private BigDecimal parseBigDecimalParam(String param) {
        if (param != null && !param.trim().isEmpty()) {
            try {
                return new BigDecimal(param.trim());
            } catch (NumberFormatException ignored) {}
        }
        return BigDecimal.ZERO;
    }
}

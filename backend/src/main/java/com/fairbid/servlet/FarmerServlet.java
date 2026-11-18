package com.fairbid.servlet;

import com.fairbid.model.ProduceLot;
import com.fairbid.service.AuctionService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Farmer Servlet providing summary metrics and earnings for the Farmer Dashboard.
 */
@WebServlet(name = "FarmerServlet", urlPatterns = {"/api/farmer/*"})
public class FarmerServlet extends BaseServlet {
    private final AuctionService auctionService = new AuctionService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        try {
            if ("/earnings".equals(pathInfo) || "/summary".equals(pathInfo)) {
                int farmerId = getAuthenticatedUserId(req);
                List<ProduceLot> lots = auctionService.getFarmerLots(farmerId);
                BigDecimal totalEarnings = auctionService.getFarmerEarnings(farmerId);

                long openCount = lots.stream().filter(l -> l.getStatus() == com.fairbid.model.LotStatus.IN_AUCTION).count();
                long soldCount = lots.stream().filter(l -> l.getStatus() == com.fairbid.model.LotStatus.SOLD || l.getStatus() == com.fairbid.model.LotStatus.SOLD_TO_AGENT).count();
                long unsoldCount = lots.stream().filter(l -> l.getStatus() == com.fairbid.model.LotStatus.UNSOLD).count();
                long availableCount = lots.stream().filter(l -> l.getStatus() == com.fairbid.model.LotStatus.AVAILABLE).count();

                Map<String, Object> summary = new HashMap<>();
                summary.put("totalLots", lots.size());
                summary.put("openAuctions", openCount);
                summary.put("soldLots", soldCount);
                summary.put("unsoldLots", unsoldCount);
                summary.put("availableLots", availableCount);
                summary.put("totalEarnings", totalEarnings);

                sendSuccess(resp, summary);
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown farmer endpoint", null);
            }
        } catch (Exception e) {
            handleException(resp, e);
        }
    }
}

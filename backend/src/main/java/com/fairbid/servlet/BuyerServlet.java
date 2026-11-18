package com.fairbid.servlet;

import com.fairbid.interfaces.Biddable;
import com.fairbid.model.AuctionResult;
import com.fairbid.model.Bid;
import com.fairbid.service.AuctionService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Buyer Servlet providing buyer-specific dashboard views: active bids, won auctions, and activity.
 */
@WebServlet(name = "BuyerServlet", urlPatterns = {"/api/buyer/*"})
public class BuyerServlet extends BaseServlet {
    private final AuctionService auctionService = new AuctionService();
    // Biddable interface usage
    private final Biddable biddableEngine = auctionService.getAuctionManager();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        try {
            int buyerId = getAuthenticatedUserId(req);
            if ("/bids".equals(pathInfo)) {
                List<Bid> bids = biddableEngine.getBidHistory(buyerId);
                sendSuccess(resp, bids);
            } else if ("/won".equals(pathInfo)) {
                List<AuctionResult> won = auctionService.getWonAuctions(buyerId);
                sendSuccess(resp, won);
            } else if ("/summary".equals(pathInfo)) {
                List<Bid> bids = biddableEngine.getBidHistory(buyerId);
                List<AuctionResult> won = auctionService.getWonAuctions(buyerId);

                long winningBidsCount = bids.stream()
                        .filter(b -> b.getStatus() == com.fairbid.model.BidStatus.WINNING)
                        .count();

                Map<String, Object> summary = new HashMap<>();
                summary.put("totalBidsPlaced", bids.size());
                summary.put("winningActiveBids", winningBidsCount);
                summary.put("wonAuctionsCount", won.size());
                summary.put("wonAuctions", won);
                summary.put("recentBids", bids.stream().limit(10).toList());

                sendSuccess(resp, summary);
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown buyer endpoint", null);
            }
        } catch (Exception e) {
            handleException(resp, e);
        }
    }
}

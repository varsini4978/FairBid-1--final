package com.fairbid.servlet;

import com.fairbid.exception.ValidationException;
import com.fairbid.interfaces.Biddable;
import com.fairbid.model.Bid;
import com.fairbid.service.AuctionService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Bid Servlet handling competitive bid submissions.
 * Multiple Interface Usage: Calls engine via Biddable interface reference.
 * Identity Security: Buyer ID extracted exclusively from authenticated session.
 */
// Note: Bid submissions are also handled directly in AuctionServlet (/api/auctions/{id}/bids)
// because Servlet 6.0 path mapping does not support wildcards in the middle of a pattern.
// This servlet demonstrates Biddable interface usage and provides an alternative direct endpoint.
@WebServlet(name = "BidServlet", urlPatterns = {"/api/bid-submission/*"})
public class BidServlet extends BaseServlet {
    private final AuctionService auctionService = new AuctionService();
    // Biddable interface reference
    private final Biddable biddableEngine = auctionService.getAuctionManager();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        try {
            // Path structure: /{id}/bids
            String[] parts = pathInfo != null ? pathInfo.split("/") : new String[0];
            if (parts.length >= 2) {
                int auctionId = Integer.parseInt(parts[1]);
                int buyerId = getAuthenticatedUserId(req);

                @SuppressWarnings("unchecked")
                Map<String, Object> body = parseJsonBody(req, Map.class);
                if (body == null || body.get("amount") == null) {
                    throw new ValidationException("Bid amount is required");
                }

                BigDecimal amount = new BigDecimal(body.get("amount").toString());

                // Interface method dispatch
                Bid placed = biddableEngine.placeBid(auctionId, buyerId, amount);
                sendCreated(resp, "Bid placed successfully! You are currently the highest bidder.", placed);
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Invalid auction bidding endpoint", null);
            }
        } catch (Exception e) {
            handleException(resp, e);
        }
    }
}

package com.fairbid.filter;

import com.fairbid.model.Role;
import com.fairbid.model.User;
import com.fairbid.util.JsonUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;

/**
 * Role-Based Access Control Filter.
 * Restricts Farmer operations (listing, closing, reopening, revising price, deciding on offers)
 * to FARMER accounts, and Buyer operations (bidding, bid history) to BUYER accounts.
 */
@WebFilter(filterName = "RoleFilter", urlPatterns = {"/api/*"})
public class RoleFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getRequestURI();
        String contextPath = req.getContextPath();
        String relativePath = path.substring(contextPath.length());
        String method = req.getMethod();

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        // Only enforce role checks if user is logged in
        if (user != null) {
            Role role = user.getRole();

            // Farmer-only operations
            if (isFarmerEndpoint(relativePath, method) && role != Role.FARMER) {
                sendForbidden(resp, "Forbidden: This action is restricted to registered Farmers only.");
                return;
            }

            // Buyer-only operations
            if (isBuyerEndpoint(relativePath, method) && role != Role.BUYER) {
                sendForbidden(resp, "Forbidden: This action is restricted to registered Buyers only.");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isFarmerEndpoint(String path, String method) {
        if ("/api/produce/mine".equals(path)) return true;
        if (path.startsWith("/api/farmer/")) return true;
        if ("POST".equalsIgnoreCase(method)) {
            if ("/api/produce".equals(path)) return true;
            if (path.matches("^/api/produce/\\d+/start-auction$")) return true;
            if (path.matches("^/api/produce/\\d+/reopen$")) return true;
            if (path.matches("^/api/produce/\\d+/revise-price$")) return true;
            if (path.matches("^/api/produce/\\d+/agent-decision$")) return true;
            if (path.matches("^/api/auctions/\\d+/close$")) return true;
        }
        return false;
    }

    private boolean isBuyerEndpoint(String path, String method) {
        if (path.startsWith("/api/buyer/")) return true;
        if ("POST".equalsIgnoreCase(method)) {
            if (path.matches("^/api/auctions/\\d+/bids$")) return true;
        }
        return false;
    }

    private void sendForbidden(HttpServletResponse resp, String message) throws IOException {
        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(JsonUtil.toJson(Map.of(
                "success", false,
                "message", message
        )));
    }

    @Override
    public void destroy() {
    }
}

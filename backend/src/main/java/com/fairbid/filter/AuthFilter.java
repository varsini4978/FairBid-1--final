package com.fairbid.filter;

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
 * Authentication Filter intercepting all /api/* requests.
 * Enforces session authentication for protected endpoints while allowing
 * public read-only access to registration, login, and produce browsing.
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {"/api/*"})
public class AuthFilter implements Filter {

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

        // Whitelisted public endpoints
        if (isPublicEndpoint(relativePath, method)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(JsonUtil.toJson(Map.of(
                    "success", false,
                    "message", "Authentication required. Please log in to proceed."
            )));
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String path, String method) {
        if ("/api/auth/login".equals(path) || "/api/auth/register".equals(path)) {
            return true;
        }
        if ("GET".equalsIgnoreCase(method)) {
            // Public browse endpoints
            if ("/api/produce".equals(path) || "/api/auctions".equals(path)) {
                return true;
            }
            if (path.startsWith("/api/produce/") && !path.equals("/api/produce/mine")) {
                return true;
            }
            if (path.startsWith("/api/auctions/")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroy() {
    }
}

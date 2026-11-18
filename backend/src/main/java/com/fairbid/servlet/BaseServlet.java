package com.fairbid.servlet;

import com.fairbid.exception.FairBidException;
import com.fairbid.exception.ValidationException;
import com.fairbid.model.Role;
import com.fairbid.model.User;
import com.fairbid.util.JsonUtil;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract Base Servlet providing unified JSON response helpers,
 * request body parsing, session extraction, and centralized exception handling.
 */
public abstract class BaseServlet extends HttpServlet {
    protected final Logger logger = Logger.getLogger(getClass().getName());

    protected void sendSuccess(HttpServletResponse resp, Object data) throws IOException {
        sendJson(resp, HttpServletResponse.SC_OK, createEnvelope(true, "Operation successful", data, null));
    }

    protected void sendCreated(HttpServletResponse resp, String message, Object data) throws IOException {
        sendJson(resp, HttpServletResponse.SC_CREATED, createEnvelope(true, message, data, null));
    }

    protected void sendError(HttpServletResponse resp, int statusCode, String message, Object errors) throws IOException {
        sendJson(resp, statusCode, createEnvelope(false, message, null, errors));
    }

    private Map<String, Object> createEnvelope(boolean success, String message, Object data, Object errors) {
        Map<String, Object> envelope = new HashMap<>();
        envelope.put("success", success);
        envelope.put("message", message);
        if (data != null) {
            envelope.put("data", data);
        }
        if (errors != null) {
            envelope.put("errors", errors);
        }
        return envelope;
    }

    private void sendJson(HttpServletResponse resp, int statusCode, Object body) throws IOException {
        resp.setStatus(statusCode);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        // Security header
        resp.setHeader("X-Content-Type-Options", "nosniff");
        try (PrintWriter writer = resp.getWriter()) {
            writer.write(JsonUtil.toJson(body));
            writer.flush();
        }
    }

    protected <T> T parseJsonBody(HttpServletRequest req, Class<T> clazz) throws IOException {
        try (BufferedReader reader = req.getReader()) {
            return JsonUtil.fromJson(reader, clazz);
        }
    }

    protected User getAuthenticatedUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            return (User) session.getAttribute("user");
        }
        return null;
    }

    protected int getAuthenticatedUserId(HttpServletRequest req) {
        User user = getAuthenticatedUser(req);
        if (user != null) {
            return user.getId();
        }
        return -1;
    }

    protected Role getAuthenticatedRole(HttpServletRequest req) {
        User user = getAuthenticatedUser(req);
        if (user != null) {
            return user.getRole();
        }
        return null;
    }

    /**
     * Centralized exception-to-HTTP mapping.
     */
    protected void handleException(HttpServletResponse resp, Exception ex) throws IOException {
        if (ex instanceof ValidationException ve) {
            logger.log(Level.INFO, "Validation error: " + ve.getMessage());
            sendError(resp, ve.getStatusCode(), ve.getMessage(), ve.getFieldErrors());
        } else if (ex instanceof FairBidException fbe) {
            logger.log(Level.INFO, "Domain error (" + fbe.getStatusCode() + "): " + fbe.getMessage());
            sendError(resp, fbe.getStatusCode(), fbe.getMessage(), null);
        } else if (ex instanceof IllegalArgumentException iae) {
            logger.log(Level.INFO, "Illegal argument error: " + iae.getMessage());
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, iae.getMessage(), null);
        } else {
            logger.log(Level.SEVERE, "Unexpected server error", ex);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "An internal server error occurred. Please try again later.", null);
        }
    }
}

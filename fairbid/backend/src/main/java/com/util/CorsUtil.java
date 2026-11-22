package com.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CorsUtil {

    public static void setCorsHeaders(HttpServletRequest req, HttpServletResponse resp) {
        String origin = req.getHeader("Origin");
        // Allow any origin (especially when files are opened via file:// or other ports)
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
    }

    public static void handleOptions(HttpServletRequest req, HttpServletResponse resp) {
        setCorsHeaders(req, resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    public static void writeJsonError(HttpServletResponse resp, int statusCode, String message) throws IOException {
        resp.setStatus(statusCode);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        String safeMsg = message == null ? "" : message.replace("\"", "\\\"");
        resp.getWriter().write("{\"success\": false, \"message\": \"" + safeMsg + "\"}");
    }
}

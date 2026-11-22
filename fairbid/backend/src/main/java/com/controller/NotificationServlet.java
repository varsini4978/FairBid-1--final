package com.controller;

import com.dao.NotificationDAO;
import com.model.Notification;
import com.util.CorsUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

@WebServlet({"/api/notifications", "/api/notifications/*"})
public class NotificationServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        CorsUtil.setCorsHeaders(req, resp);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            String farmerId = req.getParameter("farmerId");
            if (farmerId == null || farmerId.trim().isEmpty()) {
                CorsUtil.writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Parameter farmerId is required.");
                return;
            }

            farmerId = farmerId.trim();
            List<Notification> list = NotificationDAO.getNotificationsByFarmer(farmerId);
            int unread = NotificationDAO.getUnreadCount(farmerId);

            JsonObject out = new JsonObject();
            out.addProperty("success", true);
            out.add("notifications", gson.toJsonTree(list));
            out.addProperty("unreadCount", unread);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(gson.toJson(out));
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
            try (BufferedReader r = req.getReader()) {
                while ((line = r.readLine()) != null) {
                    sb.append(line);
                }
            }

            JsonObject json = JsonParser.parseString(sb.toString()).getAsJsonObject();
            String farmerId = json.has("farmerId") ? json.get("farmerId").getAsString().trim() : "";
            boolean markAll = json.has("all") && json.get("all").getAsBoolean();
            int notificationId = json.has("notificationId") ? json.get("notificationId").getAsInt() : 0;

            if (farmerId.isEmpty()) {
                CorsUtil.writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "farmerId is required.");
                return;
            }

            boolean ok;
            if (markAll || notificationId <= 0) {
                ok = NotificationDAO.markAllAsRead(farmerId);
            } else {
                ok = NotificationDAO.markAsRead(notificationId, farmerId);
            }

            int unread = NotificationDAO.getUnreadCount(farmerId);
            JsonObject out = new JsonObject();
            out.addProperty("success", ok);
            out.addProperty("unreadCount", unread);
            out.addProperty("message", ok ? "Notifications updated." : "Failed to update notification.");

            resp.setStatus(HttpServletResponse.SC_OK);
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

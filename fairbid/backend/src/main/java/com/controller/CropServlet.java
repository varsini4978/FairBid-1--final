package com.controller;

import com.dao.CropDAO;
import com.model.Crop;
import com.util.CorsUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet({"/api/crops", "/api/crops/*"})
public class CropServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        CorsUtil.setCorsHeaders(req, resp);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            String pathInfo = req.getPathInfo();
            String idParam = req.getParameter("id");
            if (idParam == null) idParam = req.getParameter("cropId");

            // Check if path has /api/crops/{id}
            if (idParam == null && pathInfo != null && pathInfo.length() > 1) {
                String sub = pathInfo.substring(1);
                if (sub.matches("\\d+")) {
                    idParam = sub;
                }
            }

            if (idParam != null && !idParam.trim().isEmpty()) {
                int cropId = Integer.parseInt(idParam.trim());
                Crop crop = CropDAO.getCropById(cropId);
                if (crop != null) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    JsonObject out = new JsonObject();
                    out.addProperty("success", true);
                    out.add("crop", gson.toJsonTree(crop));
                    resp.getWriter().write(gson.toJson(out));
                } else {
                    CorsUtil.writeJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Produce listing not found.");
                }
                return;
            }

            String farmerId = req.getParameter("farmerId");
            if (farmerId != null && !farmerId.trim().isEmpty()) {
                List<Crop> crops = CropDAO.getCropsByFarmer(farmerId.trim());
                resp.setStatus(HttpServletResponse.SC_OK);
                JsonObject out = new JsonObject();
                out.addProperty("success", true);
                out.add("crops", gson.toJsonTree(crops));
                resp.getWriter().write(gson.toJson(out));
                return;
            }

            // Marketplace open crops query with search and location filters
            String search = req.getParameter("search");
            if (search == null) search = req.getParameter("crop");
            if (search == null) search = req.getParameter("category");

            String location = req.getParameter("location");
            if (location == null) location = req.getParameter("state");

            List<Crop> openCrops = CropDAO.getOpenCrops(search, location);
            resp.setStatus(HttpServletResponse.SC_OK);
            JsonObject out = new JsonObject();
            out.addProperty("success", true);
            out.add("crops", gson.toJsonTree(openCrops));
            resp.getWriter().write(gson.toJson(out));

        } catch (NumberFormatException e) {
            CorsUtil.writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid crop ID format.");
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
            try (java.io.BufferedReader r = req.getReader()) {
                while ((line = r.readLine()) != null) {
                    sb.append(line);
                }
            }
            JsonObject json = JsonParser.parseString(sb.toString()).getAsJsonObject();

            String farmerId = json.has("farmerId") ? json.get("farmerId").getAsString().trim() : "";
            String cropName = json.has("cropName") ? json.get("cropName").getAsString().trim() : "";
            int quantity = json.has("quantity") ? json.get("quantity").getAsInt() : 0;
            double minPrice = json.has("minPrice") ? json.get("minPrice").getAsDouble() : 0.0;
            String location = json.has("location") ? json.get("location").getAsString().trim() : "";

            if (farmerId.isEmpty() || cropName.isEmpty() || quantity <= 0 || minPrice <= 0 || location.isEmpty()) {
                CorsUtil.writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid crop details. Crop name, quantity (>0), minimum price (>0), and location are required.");
                return;
            }

            Crop crop = new Crop(farmerId, cropName, quantity, minPrice, location);

            if (json.has("farmerName")) {
                crop.setFarmerName(json.get("farmerName").getAsString().trim());
            }
            if (json.has("qualityGrade")) {
                crop.setQualityGrade(json.get("qualityGrade").getAsString().trim());
            }
            if (json.has("description")) {
                crop.setDescription(json.get("description").getAsString().trim());
            }

            int cropId = CropDAO.saveCrop(crop);

            if (cropId > 0) {
                resp.setStatus(HttpServletResponse.SC_OK);
                JsonObject out = new JsonObject();
                out.addProperty("success", true);
                out.addProperty("cropId", cropId);
                out.addProperty("message", "Produce lot listed successfully for competitive bidding!");
                resp.getWriter().write(gson.toJson(out));
            } else {
                CorsUtil.writeJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to save crop in database.");
            }

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

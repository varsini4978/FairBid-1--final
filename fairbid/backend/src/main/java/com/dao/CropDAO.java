package com.dao;

import com.model.Crop;
import com.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CropDAO {

    public static int saveCrop(Crop c) {
        String farmerName = c.getFarmerName();
        if (farmerName == null || farmerName.trim().isEmpty()) {
            farmerName = getFarmerNameById(c.getFarmerId());
        }

        String qualityGrade = c.getQualityGrade();
        if (qualityGrade == null || qualityGrade.trim().isEmpty()) {
            qualityGrade = "Grade A";
        }

        String sql = "INSERT INTO crops (farmer_id, crop_name, quantity, min_price, location, status, farmer_name, quality_grade, description) " +
                     "VALUES (?, ?, ?, ?, ?, 'OPEN', ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getFarmerId());
            ps.setString(2, c.getCropName());
            ps.setInt(3, c.getQuantity());
            ps.setDouble(4, c.getMinPrice());
            ps.setString(5, c.getLocation());
            ps.setString(6, farmerName);
            ps.setString(7, qualityGrade);
            ps.setString(8, c.getDescription());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int generatedId = rs.getInt(1);
                        c.setId(generatedId);
                        return generatedId;
                    }
                }
                return 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static List<Crop> getOpenCrops(String cropQuery, String locationQuery) {
        List<Crop> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM crops WHERE status = 'OPEN'");
        List<String> params = new ArrayList<>();

        if (cropQuery != null && !cropQuery.trim().isEmpty() && !cropQuery.equalsIgnoreCase("ALL")) {
            sql.append(" AND (crop_name LIKE ? OR quality_grade LIKE ?)");
            params.add("%" + cropQuery.trim() + "%");
            params.add("%" + cropQuery.trim() + "%");
        }
        if (locationQuery != null && !locationQuery.trim().isEmpty() && !locationQuery.equalsIgnoreCase("ALL")) {
            sql.append(" AND location LIKE ?");
            params.add("%" + locationQuery.trim() + "%");
        }
        sql.append(" ORDER BY id DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCrop(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Crop> getCropsByFarmer(String farmerId) {
        List<Crop> list = new ArrayList<>();
        String sql = "SELECT * FROM crops WHERE farmer_id = ? ORDER BY id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, farmerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCrop(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Crop getCropById(int id) {
        String sql = "SELECT * FROM crops WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCrop(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getFarmerNameById(String farmerId) {
        if (farmerId == null) return null;
        String sql = "SELECT full_name FROM farmers WHERE farmer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, farmerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("full_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Farmer " + farmerId;
    }

    private static Crop mapResultSetToCrop(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String farmerId = rs.getString("farmer_id");
        String cropName = rs.getString("crop_name");
        int quantity = rs.getInt("quantity");
        double minPrice = rs.getDouble("min_price");
        String location = rs.getString("location");
        String status = rs.getString("status");
        Timestamp createdAt = rs.getTimestamp("created_at");
        String farmerName = rs.getString("farmer_name");
        String qualityGrade = rs.getString("quality_grade");
        String description = rs.getString("description");
        int acceptedBidId = rs.getInt("accepted_bid_id");
        Integer acceptedBidIdObj = rs.wasNull() ? null : acceptedBidId;
        double agreedPrice = rs.getDouble("agreed_price");
        Double agreedPriceObj = rs.wasNull() ? null : agreedPrice;
        Timestamp soldAt = rs.getTimestamp("sold_at");

        return new Crop(id, farmerId, cropName, quantity, minPrice, location, status,
                        createdAt, farmerName, qualityGrade, description, acceptedBidIdObj, agreedPriceObj, soldAt);
    }
}

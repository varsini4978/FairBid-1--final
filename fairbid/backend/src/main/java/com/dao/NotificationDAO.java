package com.dao;

import com.model.Notification;
import com.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public static boolean createNotification(String farmerId, int cropId, int bidId, String buyerName, String cropName, double bidAmount, boolean isBelowMsp) {
        String sql = "INSERT INTO notifications (farmer_id, crop_id, bid_id, buyer_name, crop_name, bid_amount, is_below_msp, is_read) VALUES (?, ?, ?, ?, ?, ?, ?, FALSE)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, farmerId);
            ps.setInt(2, cropId);
            ps.setInt(3, bidId);
            ps.setString(4, buyerName);
            ps.setString(5, cropName);
            ps.setDouble(6, bidAmount);
            ps.setBoolean(7, isBelowMsp);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<Notification> getNotificationsByFarmer(String farmerId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT id, farmer_id, crop_id, bid_id, buyer_name, crop_name, bid_amount, is_below_msp, is_read, created_at " +
                     "FROM notifications WHERE farmer_id = ? ORDER BY created_at DESC LIMIT 50";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, farmerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Notification(
                        rs.getInt("id"),
                        rs.getString("farmer_id"),
                        rs.getInt("crop_id"),
                        rs.getInt("bid_id"),
                        rs.getString("buyer_name"),
                        rs.getString("crop_name"),
                        rs.getDouble("bid_amount"),
                        rs.getBoolean("is_below_msp"),
                        rs.getBoolean("is_read"),
                        rs.getTimestamp("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static int getUnreadCount(String farmerId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE farmer_id = ? AND is_read = FALSE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, farmerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static boolean markAsRead(int notificationId, String farmerId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE id = ? AND farmer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            ps.setString(2, farmerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean markAllAsRead(String farmerId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE farmer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, farmerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

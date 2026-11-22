package com.dao;

import com.util.DBConnection;
import java.sql.*;

public class VerificationDAO {

    public static boolean updateBuyerVerification(String email, String status, String type) {
        String sql = "UPDATE buyers SET verification_status = ?, verification_type = ?, verified_at = CURRENT_TIMESTAMP WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, type);
            ps.setString(3, email);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateFarmerVerification(String farmerId, String status, String type) {
        String sql = "UPDATE farmers SET verification_status = ?, verification_type = ?, verified_at = CURRENT_TIMESTAMP WHERE farmer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, type);
            ps.setString(3, farmerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

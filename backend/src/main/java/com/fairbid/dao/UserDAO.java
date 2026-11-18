package com.fairbid.dao;

import com.fairbid.exception.DuplicateUserException;
import com.fairbid.exception.FairBidException;
import com.fairbid.model.Buyer;
import com.fairbid.model.Farmer;
import com.fairbid.model.Role;
import com.fairbid.model.User;

import java.sql.*;
import java.util.logging.Level;

/**
 * Data Access Object for Users and role-specific profiles (Farmer, Buyer).
 * Polymorphism: Methods return base User references instantiated as Farmer or Buyer.
 */
public class UserDAO extends BaseDAO {

    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking user existence by email: " + email, e);
            throw new FairBidException("Database error verifying email", e, 500);
        }
    }

    public User findByEmail(String email) {
        String sql = "SELECT u.id, u.role, u.full_name, u.email, u.password_hash, u.phone, u.location, u.created_at, " +
                "fp.farm_name, fp.village, fp.district, " +
                "bp.business_name, bp.buyer_type " +
                "FROM users u " +
                "LEFT JOIN farmer_profiles fp ON u.id = fp.user_id " +
                "LEFT JOIN buyer_profiles bp ON u.id = bp.user_id " +
                "WHERE u.email = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapUserFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error loading user by email: " + email, e);
            throw new FairBidException("Database error finding user", e, 500);
        }
        return null;
    }

    public User findById(int id) {
        String sql = "SELECT u.id, u.role, u.full_name, u.email, u.password_hash, u.phone, u.location, u.created_at, " +
                "fp.farm_name, fp.village, fp.district, " +
                "bp.business_name, bp.buyer_type " +
                "FROM users u " +
                "LEFT JOIN farmer_profiles fp ON u.id = fp.user_id " +
                "LEFT JOIN buyer_profiles bp ON u.id = bp.user_id " +
                "WHERE u.id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapUserFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error loading user by ID: " + id, e);
            throw new FairBidException("Database error finding user", e, 500);
        }
        return null;
    }

    public User registerFarmer(Farmer farmer) {
        if (existsByEmail(farmer.getEmail())) {
            throw new DuplicateUserException("Email address '" + farmer.getEmail() + "' is already registered");
        }

        String userSql = "INSERT INTO users (role, full_name, email, password_hash, phone, location, created_at) " +
                "VALUES ('FARMER', ?, ?, ?, ?, ?, ?)";
        String profileSql = "INSERT INTO farmer_profiles (user_id, farm_name, village, district) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            int userId;
            try (PreparedStatement stmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, farmer.getFullName());
                stmt.setString(2, farmer.getEmail());
                stmt.setString(3, farmer.getPasswordHash());
                stmt.setString(4, farmer.getPhone());
                stmt.setString(5, farmer.getLocation());
                stmt.setTimestamp(6, farmer.getCreatedAt());
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        userId = rs.getInt(1);
                        farmer.setId(userId);
                    } else {
                        throw new SQLException("Failed to obtain generated ID for farmer user");
                    }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(profileSql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, farmer.getFarmName());
                stmt.setString(3, farmer.getVillage());
                stmt.setString(4, farmer.getDistrict());
                stmt.executeUpdate();
            }

            conn.commit();
            return farmer;
        } catch (SQLException e) {
            rollbackQuietly(conn);
            logger.log(Level.SEVERE, "Transaction rollback during farmer registration", e);
            throw new FairBidException("Database error during farmer registration", e, 500);
        } finally {
            resetAutoCommitQuietly(conn);
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public User registerBuyer(Buyer buyer) {
        if (existsByEmail(buyer.getEmail())) {
            throw new DuplicateUserException("Email address '" + buyer.getEmail() + "' is already registered");
        }

        String userSql = "INSERT INTO users (role, full_name, email, password_hash, phone, location, created_at) " +
                "VALUES ('BUYER', ?, ?, ?, ?, ?, ?)";
        String profileSql = "INSERT INTO buyer_profiles (user_id, business_name, buyer_type) VALUES (?, ?, ?)";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            int userId;
            try (PreparedStatement stmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, buyer.getFullName());
                stmt.setString(2, buyer.getEmail());
                stmt.setString(3, buyer.getPasswordHash());
                stmt.setString(4, buyer.getPhone());
                stmt.setString(5, buyer.getLocation());
                stmt.setTimestamp(6, buyer.getCreatedAt());
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        userId = rs.getInt(1);
                        buyer.setId(userId);
                    } else {
                        throw new SQLException("Failed to obtain generated ID for buyer user");
                    }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(profileSql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, buyer.getBusinessName());
                stmt.setString(3, buyer.getBuyerType());
                stmt.executeUpdate();
            }

            conn.commit();
            return buyer;
        } catch (SQLException e) {
            rollbackQuietly(conn);
            logger.log(Level.SEVERE, "Transaction rollback during buyer registration", e);
            throw new FairBidException("Database error during buyer registration", e, 500);
        } finally {
            resetAutoCommitQuietly(conn);
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public void updateProfile(User user) {
        String baseSql = "UPDATE users SET full_name = ?, phone = ?, location = ? WHERE id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(baseSql)) {
                stmt.setString(1, user.getFullName());
                stmt.setString(2, user.getPhone());
                stmt.setString(3, user.getLocation());
                stmt.setInt(4, user.getId());
                stmt.executeUpdate();
            }

            if (user instanceof Farmer farmer) {
                String farmerSql = "UPDATE farmer_profiles SET farm_name = ?, village = ?, district = ? WHERE user_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(farmerSql)) {
                    stmt.setString(1, farmer.getFarmName());
                    stmt.setString(2, farmer.getVillage());
                    stmt.setString(3, farmer.getDistrict());
                    stmt.setInt(4, farmer.getId());
                    stmt.executeUpdate();
                }
            } else if (user instanceof Buyer buyer) {
                String buyerSql = "UPDATE buyer_profiles SET business_name = ?, buyer_type = ? WHERE user_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(buyerSql)) {
                    stmt.setString(1, buyer.getBusinessName());
                    stmt.setString(2, buyer.getBuyerType());
                    stmt.setInt(3, buyer.getId());
                    stmt.executeUpdate();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            rollbackQuietly(conn);
            logger.log(Level.SEVERE, "Error updating user profile for ID: " + user.getId(), e);
            throw new FairBidException("Database error updating profile", e, 500);
        } finally {
            resetAutoCommitQuietly(conn);
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * Polymorphic Factory Method:
     * Maps database row to either Farmer or Buyer instance based on role column.
     */
    private User mapUserFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String roleStr = rs.getString("role");
        Role role = Role.valueOf(roleStr);
        String fullName = rs.getString("full_name");
        String email = rs.getString("email");
        String passwordHash = rs.getString("password_hash");
        String phone = rs.getString("phone");
        String location = rs.getString("location");
        Timestamp createdAt = rs.getTimestamp("created_at");

        if (role == Role.FARMER) {
            String farmName = rs.getString("farm_name");
            String village = rs.getString("village");
            String district = rs.getString("district");
            return new Farmer(id, fullName, email, passwordHash, phone, location, createdAt,
                    farmName, village, district);
        } else {
            String businessName = rs.getString("business_name");
            String buyerType = rs.getString("buyer_type");
            return new Buyer(id, fullName, email, passwordHash, phone, location, createdAt,
                    businessName, buyerType);
        }
    }
}

package com.dao;

import com.model.Bid;
import com.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuctionDAO {

    public static class BidResult {
        public boolean success;
        public String message;
        public int bidId;

        public BidResult(boolean success, String message, int bidId) {
            this.success = success;
            this.message = message;
            this.bidId = bidId;
        }
    }

    // ---------- Place Bid ----------
    public static boolean placeBid(int cropId, String buyerEmail, double bidAmount) {
        BidResult res = placeBidWithValidation(cropId, buyerEmail, null, bidAmount);
        return res.success;
    }

    public static BidResult placeBidWithValidation(int cropId, String buyerEmail, String buyerName, double bidAmount) {
        if (bidAmount <= 0) {
            return new BidResult(false, "Bid amount must be a positive number.", 0);
        }
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return new BidResult(false, "Database connection unavailable.", 0);
            }
            // Verify crop exists and is OPEN
            String cropSql = "SELECT status, min_price FROM crops WHERE id = ?";
            String status = null;
            double minPrice = 0.0;
            try (PreparedStatement psCrop = conn.prepareStatement(cropSql)) {
                psCrop.setInt(1, cropId);
                try (ResultSet rs = psCrop.executeQuery()) {
                    if (!rs.next()) {
                        return new BidResult(false, "Produce lot not found.", 0);
                    }
                    status = rs.getString("status");
                    minPrice = rs.getDouble("min_price");
                }
            }
            if (!"OPEN".equalsIgnoreCase(status)) {
                return new BidResult(false, "This auction is already closed or sold.", 0);
            }
            // MSP check removed – any positive bid allowed
            // Check current highest bid
            String maxBidSql = "SELECT MAX(bid_amount) as highest FROM bids WHERE crop_id = ?";
            try (PreparedStatement psMax = conn.prepareStatement(maxBidSql)) {
                psMax.setInt(1, cropId);
                try (ResultSet rs = psMax.executeQuery()) {
                    if (rs.next()) {
                        double highest = rs.getDouble("highest");
                        if (!rs.wasNull() && bidAmount <= highest) {
                            return new BidResult(false, "Bid must be strictly higher than current highest bid of ₹" + highest, 0);
                        }
                    }
                }
            }
            // Resolve buyer name if missing
            if (buyerName == null || buyerName.trim().isEmpty()) {
                String buyerSql = "SELECT name FROM buyers WHERE email = ?";
                try (PreparedStatement psBuyer = conn.prepareStatement(buyerSql)) {
                    psBuyer.setString(1, buyerEmail);
                    try (ResultSet rs = psBuyer.executeQuery()) {
                        if (rs.next()) {
                            buyerName = rs.getString("name");
                        } else {
                            buyerName = "Buyer (" + buyerEmail + ")";
                        }
                    }
                }
            }
            String insertSql = "INSERT INTO bids (crop_id, buyer_name, buyer_email, bid_amount, status) VALUES (?, ?, ?, ?, 'PENDING')";
            try (PreparedStatement psInsert = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                psInsert.setInt(1, cropId);
                psInsert.setString(2, buyerName);
                psInsert.setString(3, buyerEmail);
                psInsert.setDouble(4, bidAmount);
                int rows = psInsert.executeUpdate();
                if (rows > 0) {
                    try (ResultSet rsKeys = psInsert.getGeneratedKeys()) {
                        if (rsKeys.next()) {
                            int bidId = rsKeys.getInt(1);
                            // Notify farmer of the new bid (including below-MSP)
                            try {
                                String farmerSql = "SELECT farmer_id, crop_name, min_price FROM crops WHERE id = ?";
                                try (PreparedStatement psF = conn.prepareStatement(farmerSql)) {
                                    psF.setInt(1, cropId);
                                    try (ResultSet rsF = psF.executeQuery()) {
                                        if (rsF.next()) {
                                            String farmerId = rsF.getString("farmer_id");
                                            String cropName = rsF.getString("crop_name");
                                            double msp = rsF.getDouble("min_price");
                                            boolean isBelow = bidAmount < msp;
                                            NotificationDAO.createNotification(farmerId, cropId, bidId, buyerName, cropName, bidAmount, isBelow);
                                        }
                                    }
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            return new BidResult(true, "Bid recorded successfully!", bidId);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new BidResult(false, "Database error: " + e.getMessage(), 0);
        }
        return new BidResult(false, "Failed to record bid.", 0);
    }

    // ---------- Retrieve Bids ----------
    public static List<Bid> getBidsByCrop(int cropId) {
        List<Bid> list = new ArrayList<>();
        String sql = "SELECT * FROM bids WHERE crop_id = ? ORDER BY bid_amount DESC, created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cropId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToBid(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Bid> getBidsByBuyer(String buyerEmail) {
        List<Bid> list = new ArrayList<>();
        String sql = "SELECT b.id, b.crop_id, b.buyer_name, b.buyer_email, b.bid_amount, b.status, b.created_at, " +
                     "c.crop_name, c.quantity, c.location, c.status as crop_status " +
                     "FROM bids b JOIN crops c ON b.crop_id = c.id " +
                     "WHERE b.buyer_email = ? ORDER BY b.id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, buyerEmail);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Bid b = mapResultSetToBid(rs);
                    b.setCropName(rs.getString("crop_name"));
                    b.setQuantity(rs.getInt("quantity"));
                    b.setLocation(rs.getString("location"));
                    b.setCropStatus(rs.getString("crop_status"));
                    list.add(b);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ---------- Accept Bid ----------
    public static BidResult acceptBid(int cropId, int bidId, String farmerId) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return new BidResult(false, "Database connection unavailable.", 0);
            }
            conn.setAutoCommit(false);
            try {
                // Verify crop ownership and status
                String cropSql = "SELECT farmer_id, status FROM crops WHERE id = ? FOR UPDATE";
                String cropFarmerId = null;
                String cropStatus = null;
                try (PreparedStatement ps = conn.prepareStatement(cropSql)) {
                    ps.setInt(1, cropId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return new BidResult(false, "Produce lot not found.", 0);
                        }
                        cropFarmerId = rs.getString("farmer_id");
                        cropStatus = rs.getString("status");
                    }
                }
                if (farmerId != null && !farmerId.equals(cropFarmerId)) {
                    conn.rollback();
                    return new BidResult(false, "Unauthorized: You do not own this produce listing.", 0);
                }
                if (!"OPEN".equalsIgnoreCase(cropStatus)) {
                    conn.rollback();
                    return new BidResult(false, "Auction is already sold or closed.", 0);
                }
                // Verify bid
                String bidSql = "SELECT bid_amount, status FROM bids WHERE id = ? AND crop_id = ? FOR UPDATE";
                double agreedPrice = 0.0;
                String bidStatus = null;
                try (PreparedStatement ps = conn.prepareStatement(bidSql)) {
                    ps.setInt(1, bidId);
                    ps.setInt(2, cropId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return new BidResult(false, "Bid not found for this produce lot.", 0);
                        }
                        agreedPrice = rs.getDouble("bid_amount");
                        bidStatus = rs.getString("status");
                    }
                }
                if (!"PENDING".equalsIgnoreCase(bidStatus)) {
                    conn.rollback();
                    return new BidResult(false, "Bid is no longer pending (already " + bidStatus + ").", 0);
                }
                // Accept selected bid
                try (PreparedStatement ps = conn.prepareStatement("UPDATE bids SET status = 'ACCEPTED' WHERE id = ?")) {
                    ps.setInt(1, bidId);
                    ps.executeUpdate();
                }
                // Reject others
                try (PreparedStatement ps = conn.prepareStatement("UPDATE bids SET status = 'REJECTED' WHERE crop_id = ? AND id != ? AND status = 'PENDING'")) {
                    ps.setInt(1, cropId);
                    ps.setInt(2, bidId);
                    ps.executeUpdate();
                }
                // Update crop to SOLD
                try (PreparedStatement ps = conn.prepareStatement("UPDATE crops SET status = 'SOLD', accepted_bid_id = ?, agreed_price = ?, sold_at = CURRENT_TIMESTAMP WHERE id = ?")) {
                    ps.setInt(1, bidId);
                    ps.setDouble(2, agreedPrice);
                    ps.setInt(3, cropId);
                    ps.executeUpdate();
                }
                conn.commit();
                return new BidResult(true, "Bid accepted and produce marked SOLD at ₹" + agreedPrice, bidId);
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return new BidResult(false, "Transaction failed: " + e.getMessage(), 0);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new BidResult(false, "Database connection error: " + e.getMessage(), 0);
        }
    }

    // ---------- Reject Bid ----------
    public static BidResult rejectBid(int cropId, int bidId, String farmerId) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return new BidResult(false, "Database connection unavailable.", 0);
            }
            // Verify ownership if provided
            if (farmerId != null) {
                String verifySql = "SELECT farmer_id FROM crops WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(verifySql)) {
                    ps.setInt(1, cropId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            return new BidResult(false, "Produce lot not found.", 0);
                        }
                        if (!farmerId.equals(rs.getString("farmer_id"))) {
                            return new BidResult(false, "Unauthorized: You do not own this produce listing.", 0);
                        }
                    }
                }
            }
            String sql = "UPDATE bids SET status = 'REJECTED' WHERE id = ? AND crop_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, bidId);
                ps.setInt(2, cropId);
                int affected = ps.executeUpdate();
                if (affected > 0) {
                    return new BidResult(true, "Bid rejected. Auction remains open.", bidId);
                } else {
                    return new BidResult(false, "Bid not found or already processed.", 0);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new BidResult(false, "Database error: " + e.getMessage(), 0);
        }
    }

    // ---------- Helper Methods ----------
    private static Bid mapResultSetToBid(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int cropId = rs.getInt("crop_id");
        String buyerName = rs.getString("buyer_name");
        String buyerEmail = rs.getString("buyer_email");
        double bidAmount = rs.getDouble("bid_amount");
        String status = rs.getString("status");
        Timestamp createdAt = rs.getTimestamp("created_at");
        return new Bid(id, cropId, buyerName, buyerEmail, bidAmount, status, createdAt);
    }

    // Check if a bid is below current MSP
    public static boolean isBelowMsp(int cropId, double bidAmount) {
        String sql = "SELECT min_price FROM crops WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cropId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double msp = rs.getDouble("min_price");
                    return bidAmount < msp;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Accept bid regardless of MSP (used for "Accept Anyway")
    public static BidResult acceptBidRegardlessOfMsp(int cropId, int bidId, String farmerId) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return new BidResult(false, "Database connection unavailable.", 0);
            }
            conn.setAutoCommit(false);
            try {
                // Verify crop ownership and status
                String cropSql = "SELECT farmer_id, status FROM crops WHERE id = ? FOR UPDATE";
                String cropFarmerId = null;
                String cropStatus = null;
                try (PreparedStatement ps = conn.prepareStatement(cropSql)) {
                    ps.setInt(1, cropId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return new BidResult(false, "Produce lot not found.", 0);
                        }
                        cropFarmerId = rs.getString("farmer_id");
                        cropStatus = rs.getString("status");
                    }
                }
                if (farmerId != null && !farmerId.equals(cropFarmerId)) {
                    conn.rollback();
                    return new BidResult(false, "Unauthorized: You do not own this produce listing.", 0);
                }
                if (!"OPEN".equalsIgnoreCase(cropStatus)) {
                    conn.rollback();
                    return new BidResult(false, "Auction is already sold or closed.", 0);
                }
                // Verify bid
                String bidSql = "SELECT bid_amount, status FROM bids WHERE id = ? AND crop_id = ? FOR UPDATE";
                double agreedPrice = 0.0;
                String bidStatus = null;
                try (PreparedStatement ps = conn.prepareStatement(bidSql)) {
                    ps.setInt(1, bidId);
                    ps.setInt(2, cropId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return new BidResult(false, "Bid not found for this produce lot.", 0);
                        }
                        agreedPrice = rs.getDouble("bid_amount");
                        bidStatus = rs.getString("status");
                    }
                }
                if (!"PENDING".equalsIgnoreCase(bidStatus)) {
                    conn.rollback();
                    return new BidResult(false, "Bid is no longer pending (already " + bidStatus + ").", 0);
                }
                // Accept selected bid
                try (PreparedStatement ps = conn.prepareStatement("UPDATE bids SET status = 'ACCEPTED' WHERE id = ?")) {
                    ps.setInt(1, bidId);
                    ps.executeUpdate();
                }
                // Reject others
                try (PreparedStatement ps = conn.prepareStatement("UPDATE bids SET status = 'REJECTED' WHERE crop_id = ? AND id != ? AND status = 'PENDING'")) {
                    ps.setInt(1, cropId);
                    ps.setInt(2, bidId);
                    ps.executeUpdate();
                }
                // Update crop to SOLD
                try (PreparedStatement ps = conn.prepareStatement("UPDATE crops SET status = 'SOLD', accepted_bid_id = ?, agreed_price = ?, sold_at = CURRENT_TIMESTAMP WHERE id = ?")) {
                    ps.setInt(1, bidId);
                    ps.setDouble(2, agreedPrice);
                    ps.setInt(3, cropId);
                    ps.executeUpdate();
                }
                conn.commit();
                return new BidResult(true, "Bid accepted and produce marked SOLD at ₹" + agreedPrice, bidId);
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return new BidResult(false, "Transaction failed: " + e.getMessage(), 0);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new BidResult(false, "Database connection error: " + e.getMessage(), 0);
        }
    }

    // Revise MSP for a crop
    public static boolean reviseMsp(int cropId, double newMsp, String farmerId) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return false;
            }
            conn.setAutoCommit(false);
            try {
                // Verify ownership
                String verifySql = "SELECT farmer_id FROM crops WHERE id = ? FOR UPDATE";
                try (PreparedStatement ps = conn.prepareStatement(verifySql)) {
                    ps.setInt(1, cropId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || (farmerId != null && !farmerId.equals(rs.getString("farmer_id")))) {
                            conn.rollback();
                            return false;
                        }
                    }
                }
                String updateSql = "UPDATE crops SET min_price = ? WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setDouble(1, newMsp);
                    ps.setInt(2, cropId);
                    ps.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

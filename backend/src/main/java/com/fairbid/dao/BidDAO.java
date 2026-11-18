package com.fairbid.dao;

import com.fairbid.exception.FairBidException;
import com.fairbid.model.Bid;
import com.fairbid.model.BidStatus;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Data Access Object for Bids placed by Buyers.
 */
public class BidDAO extends BaseDAO {

    public Bid create(Connection conn, Bid bid) throws SQLException {
        String sql = "INSERT INTO bids (auction_id, buyer_id, amount_per_unit, placed_at, status) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, bid.getAuctionId());
            stmt.setInt(2, bid.getBuyerId());
            stmt.setBigDecimal(3, bid.getAmountPerUnit());
            stmt.setTimestamp(4, bid.getPlacedAt());
            stmt.setString(5, bid.getStatus().name());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    bid.setId(rs.getInt(1));
                }
            }
            return bid;
        }
    }

    public List<Bid> findBidsByAuctionId(int auctionId) {
        String sql = "SELECT b.*, u.full_name as buyer_name, bp.business_name as buyer_business " +
                "FROM bids b " +
                "JOIN users u ON b.buyer_id = u.id " +
                "LEFT JOIN buyer_profiles bp ON u.id = bp.user_id " +
                "WHERE b.auction_id = ? " +
                "ORDER BY b.amount_per_unit DESC, b.placed_at ASC";

        List<Bid> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, auctionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBidFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding bids for auction: " + auctionId, e);
            throw new FairBidException("Database error loading bids", e, 500);
        }
        return list;
    }

    public Bid findHighestValidBid(Connection conn, int auctionId) throws SQLException {
        String sql = "SELECT b.*, u.full_name as buyer_name, bp.business_name as buyer_business " +
                "FROM bids b " +
                "JOIN users u ON b.buyer_id = u.id " +
                "LEFT JOIN buyer_profiles bp ON u.id = bp.user_id " +
                "WHERE b.auction_id = ? AND b.status IN ('VALID', 'WINNING', 'OUTBID') " +
                "ORDER BY b.amount_per_unit DESC, b.placed_at ASC LIMIT 1";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, auctionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapBidFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public void updateBidStatus(Connection conn, int bidId, BidStatus status) throws SQLException {
        String sql = "UPDATE bids SET status = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, bidId);
            stmt.executeUpdate();
        }
    }

    public List<Bid> findBidsByBuyerId(int buyerId) {
        String sql = "SELECT b.*, u.full_name as buyer_name, bp.business_name as buyer_business, " +
                "p.crop_name, p.quantity, p.unit, a.round_number, a.status as auction_status, a.end_time " +
                "FROM bids b " +
                "JOIN users u ON b.buyer_id = u.id " +
                "LEFT JOIN buyer_profiles bp ON u.id = bp.user_id " +
                "JOIN auctions a ON b.auction_id = a.id " +
                "JOIN produce_lots p ON a.lot_id = p.id " +
                "WHERE b.buyer_id = ? " +
                "ORDER BY b.placed_at DESC";

        List<Bid> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, buyerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBidFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding bids for buyer: " + buyerId, e);
            throw new FairBidException("Database error loading buyer bid history", e, 500);
        }
        return list;
    }

    public int countBidsByAuctionId(int auctionId) {
        String sql = "SELECT COUNT(*) FROM bids WHERE auction_id = ? AND status != 'REJECTED'";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, auctionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error counting bids for auction: " + auctionId, e);
        }
        return 0;
    }

    private Bid mapBidFromResultSet(ResultSet rs) throws SQLException {
        Bid bid = new Bid(
                rs.getInt("id"),
                rs.getInt("auction_id"),
                rs.getInt("buyer_id"),
                rs.getBigDecimal("amount_per_unit"),
                rs.getTimestamp("placed_at"),
                BidStatus.valueOf(rs.getString("status"))
        );
        bid.setBuyerName(rs.getString("buyer_name"));
        bid.setBuyerBusiness(rs.getString("buyer_business"));
        return bid;
    }
}

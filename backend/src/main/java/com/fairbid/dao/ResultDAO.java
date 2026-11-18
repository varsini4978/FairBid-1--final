package com.fairbid.dao;

import com.fairbid.exception.FairBidException;
import com.fairbid.model.AuctionOutcome;
import com.fairbid.model.AuctionResult;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Data Access Object for settled Auction Results.
 */
public class ResultDAO extends BaseDAO {

    public AuctionResult create(Connection conn, AuctionResult result) throws SQLException {
        String sql = "INSERT INTO auction_results (auction_id, winning_bid_id, winner_buyer_id, final_price_per_unit, total_amount, outcome, closed_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, result.getAuctionId());
            if (result.getWinningBidId() != null) {
                stmt.setInt(2, result.getWinningBidId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            if (result.getWinnerBuyerId() != null) {
                stmt.setInt(3, result.getWinnerBuyerId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.setBigDecimal(4, result.getFinalPricePerUnit());
            stmt.setBigDecimal(5, result.getTotalAmount());
            stmt.setString(6, result.getOutcome().name());
            stmt.setTimestamp(7, result.getClosedAt());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    result.setId(rs.getInt(1));
                }
            }
            return result;
        }
    }

    public AuctionResult findByAuctionId(int auctionId) {
        String sql = "SELECT ar.*, u.full_name as winner_name, bp.business_name as winner_business, " +
                "p.crop_name, p.quantity, p.unit, p.agent_offer_per_unit, a.round_number, a.lot_id as lot_id, a.min_price_snapshot, " +
                "(SELECT COUNT(*) FROM bids WHERE auction_id = ar.auction_id) as total_bids " +
                "FROM auction_results ar " +
                "JOIN auctions a ON ar.auction_id = a.id " +
                "JOIN produce_lots p ON a.lot_id = p.id " +
                "LEFT JOIN users u ON ar.winner_buyer_id = u.id " +
                "LEFT JOIN buyer_profiles bp ON u.id = bp.user_id " +
                "WHERE ar.auction_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, auctionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding auction result for auction: " + auctionId, e);
            throw new FairBidException("Database error loading auction result", e, 500);
        }
        return null;
    }

    public List<AuctionResult> findWonAuctionsByBuyerId(int buyerId) {
        String sql = "SELECT ar.*, u.full_name as winner_name, bp.business_name as winner_business, " +
                "p.crop_name, p.quantity, p.unit, p.agent_offer_per_unit, a.round_number, a.lot_id as lot_id, a.min_price_snapshot, " +
                "(SELECT COUNT(*) FROM bids WHERE auction_id = ar.auction_id) as total_bids " +
                "FROM auction_results ar " +
                "JOIN auctions a ON ar.auction_id = a.id " +
                "JOIN produce_lots p ON a.lot_id = p.id " +
                "LEFT JOIN users u ON ar.winner_buyer_id = u.id " +
                "LEFT JOIN buyer_profiles bp ON u.id = bp.user_id " +
                "WHERE ar.winner_buyer_id = ? AND ar.outcome = 'SOLD' " +
                "ORDER BY ar.closed_at DESC";

        List<AuctionResult> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, buyerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding won auctions for buyer: " + buyerId, e);
            throw new FairBidException("Database error loading won auctions", e, 500);
        }
        return list;
    }

    private AuctionResult mapResultFromResultSet(ResultSet rs) throws SQLException {
        Integer winningBidId = rs.getInt("winning_bid_id");
        if (rs.wasNull()) winningBidId = null;

        Integer winnerBuyerId = rs.getInt("winner_buyer_id");
        if (rs.wasNull()) winnerBuyerId = null;

        AuctionResult result = new AuctionResult(
                rs.getInt("id"),
                rs.getInt("auction_id"),
                winningBidId,
                winnerBuyerId,
                rs.getBigDecimal("final_price_per_unit"),
                rs.getBigDecimal("total_amount"),
                AuctionOutcome.valueOf(rs.getString("outcome")),
                rs.getTimestamp("closed_at")
        );
        result.setWinnerName(rs.getString("winner_name"));
        result.setWinnerBusiness(rs.getString("winner_business"));
        result.setCropName(rs.getString("crop_name"));
        result.setQuantity(rs.getBigDecimal("quantity"));
        result.setUnit(rs.getString("unit"));
        result.setRoundNumber(rs.getInt("round_number"));
        // Extra enrichment fields for frontend
        try { result.setLotId(rs.getInt("lot_id")); } catch (SQLException ignored) {}
        try { result.setMinPriceSnapshot(rs.getBigDecimal("min_price_snapshot")); } catch (SQLException ignored) {}
        try { result.setAgentOfferPerUnit(rs.getBigDecimal("agent_offer_per_unit")); } catch (SQLException ignored) {}
        try { result.setTotalBids(rs.getInt("total_bids")); } catch (SQLException ignored) {}
        return result;
    }

    /** Find the latest closed auction result for a produce lot (used by farmer dashboard). */
    public AuctionResult findLatestResultByLotId(int lotId) {
        String sql = "SELECT ar.*, u.full_name as winner_name, bp.business_name as winner_business, " +
                "p.crop_name, p.quantity, p.unit, p.agent_offer_per_unit, a.round_number, a.lot_id as lot_id, a.min_price_snapshot, " +
                "(SELECT COUNT(*) FROM bids WHERE auction_id = ar.auction_id) as total_bids " +
                "FROM auction_results ar " +
                "JOIN auctions a ON ar.auction_id = a.id " +
                "JOIN produce_lots p ON a.lot_id = p.id " +
                "LEFT JOIN users u ON ar.winner_buyer_id = u.id " +
                "LEFT JOIN buyer_profiles bp ON u.id = bp.user_id " +
                "WHERE a.lot_id = ? " +
                "ORDER BY ar.closed_at DESC LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, lotId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding latest result for lot: " + lotId, e);
            throw new FairBidException("Database error loading lot result", e, 500);
        }
        return null;
    }
}

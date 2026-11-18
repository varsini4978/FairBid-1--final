package com.fairbid.dao;

import com.fairbid.exception.FairBidException;
import com.fairbid.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Data Access Object for Auctions.
 * Supports multi-round auctions and row-level locking (SELECT ... FOR UPDATE)
 * for concurrency-safe bidding and closing.
 */
public class AuctionDAO extends BaseDAO {

    public Auction create(Auction auction) {
        String sql = "INSERT INTO auctions (lot_id, round_number, min_price_snapshot, start_time, end_time, status, outcome) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, auction.getLotId());
            stmt.setInt(2, auction.getRoundNumber());
            stmt.setBigDecimal(3, auction.getMinPriceSnapshot());
            stmt.setTimestamp(4, auction.getStartTime());
            stmt.setTimestamp(5, auction.getEndTime());
            stmt.setString(6, auction.getStatus().name());
            stmt.setString(7, auction.getOutcome() != null ? auction.getOutcome().name() : null);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    auction.setId(rs.getInt(1));
                }
            }
            return auction;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating auction round", e);
            throw new FairBidException("Database error starting auction round", e, 500);
        }
    }

    public Auction findById(int id) {
        String sql = "SELECT a.*, p.crop_name, p.category, p.quantity, p.unit, p.agent_offer_per_unit, " +
                "p.farmer_id, u.full_name as farmer_name, u.location as farmer_location " +
                "FROM auctions a " +
                "JOIN produce_lots p ON a.lot_id = p.id " +
                "JOIN users u ON p.farmer_id = u.id " +
                "WHERE a.id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapAuctionFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding auction by ID: " + id, e);
            throw new FairBidException("Database error finding auction", e, 500);
        }
        return null;
    }

    /**
     * Concurrency Safety:
     * Locks the auction row using SELECT ... FOR UPDATE within an active transaction.
     */
    public Auction findByIdForUpdate(Connection conn, int id) throws SQLException {
        String sql = "SELECT a.*, p.crop_name, p.category, p.quantity, p.unit, p.agent_offer_per_unit, " +
                "p.farmer_id, u.full_name as farmer_name, u.location as farmer_location " +
                "FROM auctions a " +
                "JOIN produce_lots p ON a.lot_id = p.id " +
                "JOIN users u ON p.farmer_id = u.id " +
                "WHERE a.id = ? FOR UPDATE";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapAuctionFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public Auction findActiveByLotId(int lotId) {
        String sql = "SELECT a.*, p.crop_name, p.category, p.quantity, p.unit, p.agent_offer_per_unit, " +
                "p.farmer_id, u.full_name as farmer_name, u.location as farmer_location " +
                "FROM auctions a " +
                "JOIN produce_lots p ON a.lot_id = p.id " +
                "JOIN users u ON p.farmer_id = u.id " +
                "WHERE a.lot_id = ? AND a.status = 'OPEN' " +
                "ORDER BY a.round_number DESC LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, lotId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapAuctionFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding active auction for lot: " + lotId, e);
            throw new FairBidException("Database error finding active auction", e, 500);
        }
        return null;
    }

    public List<Auction> findByLotId(int lotId) {
        String sql = "SELECT a.*, p.crop_name, p.category, p.quantity, p.unit, p.agent_offer_per_unit, " +
                "p.farmer_id, u.full_name as farmer_name, u.location as farmer_location " +
                "FROM auctions a " +
                "JOIN produce_lots p ON a.lot_id = p.id " +
                "JOIN users u ON p.farmer_id = u.id " +
                "WHERE a.lot_id = ? " +
                "ORDER BY a.round_number ASC";

        List<Auction> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, lotId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAuctionFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding auction rounds for lot: " + lotId, e);
            throw new FairBidException("Database error finding auction rounds", e, 500);
        }
        return list;
    }

    public List<Auction> findAllOpen(String category, String search) {
        StringBuilder sql = new StringBuilder(
                "SELECT a.*, p.crop_name, p.category, p.quantity, p.unit, p.agent_offer_per_unit, " +
                "p.farmer_id, u.full_name as farmer_name, u.location as farmer_location " +
                "FROM auctions a " +
                "JOIN produce_lots p ON a.lot_id = p.id " +
                "JOIN users u ON p.farmer_id = u.id " +
                "WHERE a.status = 'OPEN' AND a.end_time > NOW() "
        );

        List<Object> params = new ArrayList<>();
        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("ALL")) {
            sql.append("AND p.category = ? ");
            params.add(category.trim());
        }
        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (p.crop_name LIKE ? OR u.location LIKE ?) ");
            String likeParam = "%" + search.trim() + "%";
            params.add(likeParam);
            params.add(likeParam);
        }
        sql.append("ORDER BY a.end_time ASC");

        List<Auction> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAuctionFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error browsing open auctions", e);
            throw new FairBidException("Database error loading open auctions", e, 500);
        }
        return list;
    }

    public List<Auction> findExpiredOpenAuctions() {
        String sql = "SELECT a.*, p.crop_name, p.category, p.quantity, p.unit, p.agent_offer_per_unit, " +
                "p.farmer_id, u.full_name as farmer_name, u.location as farmer_location " +
                "FROM auctions a " +
                "JOIN produce_lots p ON a.lot_id = p.id " +
                "JOIN users u ON p.farmer_id = u.id " +
                "WHERE a.status = 'OPEN' AND a.end_time <= NOW()";

        List<Auction> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapAuctionFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error querying expired open auctions", e);
        }
        return list;
    }

    public void closeAuction(Connection conn, int auctionId, AuctionOutcome outcome) throws SQLException {
        String sql = "UPDATE auctions SET status = 'CLOSED', outcome = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, outcome.name());
            stmt.setInt(2, auctionId);
            stmt.executeUpdate();
        }
    }

    private Auction mapAuctionFromResultSet(ResultSet rs) throws SQLException {
        Auction auction = new Auction(
                rs.getInt("id"),
                rs.getInt("lot_id"),
                rs.getInt("round_number"),
                rs.getBigDecimal("min_price_snapshot"),
                rs.getTimestamp("start_time"),
                rs.getTimestamp("end_time"),
                AuctionStatus.valueOf(rs.getString("status")),
                rs.getString("outcome") != null ? AuctionOutcome.valueOf(rs.getString("outcome")) : null
        );

        ProduceLot lot = new ProduceLot(
                rs.getInt("lot_id"),
                rs.getInt("farmer_id"),
                rs.getString("crop_name"),
                rs.getString("category"),
                rs.getBigDecimal("quantity"),
                rs.getString("unit"),
                rs.getBigDecimal("min_price_snapshot"),
                rs.getBigDecimal("agent_offer_per_unit"),
                LotStatus.IN_AUCTION,
                rs.getTimestamp("start_time")
        );
        lot.setFarmerName(rs.getString("farmer_name"));
        lot.setFarmerLocation(rs.getString("farmer_location"));
        auction.setProduceLot(lot);

        return auction;
    }
}

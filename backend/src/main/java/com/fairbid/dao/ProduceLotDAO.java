package com.fairbid.dao;

import com.fairbid.exception.FairBidException;
import com.fairbid.model.LotStatus;
import com.fairbid.model.ProduceLot;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Data Access Object for Produce Lots listed by Farmers.
 */
public class ProduceLotDAO extends BaseDAO {

    public ProduceLot create(ProduceLot lot) {
        String sql = "INSERT INTO produce_lots (farmer_id, crop_name, category, quantity, unit, min_price_per_unit, agent_offer_per_unit, status, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, lot.getFarmerId());
            stmt.setString(2, lot.getCropName());
            stmt.setString(3, lot.getCategory());
            stmt.setBigDecimal(4, lot.getQuantity());
            stmt.setString(5, lot.getUnit());
            stmt.setBigDecimal(6, lot.getMinPricePerUnit());
            stmt.setBigDecimal(7, lot.getAgentOfferPerUnit() != null ? lot.getAgentOfferPerUnit() : BigDecimal.ZERO);
            stmt.setString(8, lot.getStatus().name());
            stmt.setTimestamp(9, lot.getCreatedAt());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    lot.setId(rs.getInt(1));
                }
            }
            return lot;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating produce lot", e);
            throw new FairBidException("Database error saving produce lot", e, 500);
        }
    }

    public ProduceLot findById(int id) {
        String sql = "SELECT p.*, u.full_name as farmer_name, u.location as farmer_location, " +
                "(SELECT a.id FROM auctions a WHERE a.lot_id = p.id AND a.status = 'OPEN' ORDER BY a.round_number DESC LIMIT 1) as active_auction_id, " +
                "(SELECT MAX(b.amount_per_unit) FROM bids b JOIN auctions a ON b.auction_id = a.id WHERE a.lot_id = p.id AND a.status = 'OPEN') as highest_bid " +
                "FROM produce_lots p " +
                "JOIN users u ON p.farmer_id = u.id " +
                "WHERE p.id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapLotFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding produce lot by ID: " + id, e);
            throw new FairBidException("Database error finding produce lot", e, 500);
        }
        return null;
    }

    public List<ProduceLot> findByFarmerId(int farmerId) {
        String sql = "SELECT p.*, u.full_name as farmer_name, u.location as farmer_location, " +
                "(SELECT a.id FROM auctions a WHERE a.lot_id = p.id AND a.status = 'OPEN' ORDER BY a.round_number DESC LIMIT 1) as active_auction_id, " +
                "(SELECT MAX(b.amount_per_unit) FROM bids b JOIN auctions a ON b.auction_id = a.id WHERE a.lot_id = p.id AND a.status = 'OPEN') as highest_bid " +
                "FROM produce_lots p " +
                "JOIN users u ON p.farmer_id = u.id " +
                "WHERE p.farmer_id = ? " +
                "ORDER BY p.created_at DESC";

        List<ProduceLot> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, farmerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapLotFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error listing produce lots for farmer: " + farmerId, e);
            throw new FairBidException("Database error loading farmer lots", e, 500);
        }
        return list;
    }

    public List<ProduceLot> findAll(String category, String search) {
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, u.full_name as farmer_name, u.location as farmer_location, " +
                "(SELECT a.id FROM auctions a WHERE a.lot_id = p.id AND a.status = 'OPEN' ORDER BY a.round_number DESC LIMIT 1) as active_auction_id, " +
                "(SELECT MAX(b.amount_per_unit) FROM bids b JOIN auctions a ON b.auction_id = a.id WHERE a.lot_id = p.id AND a.status = 'OPEN') as highest_bid " +
                "FROM produce_lots p " +
                "JOIN users u ON p.farmer_id = u.id " +
                "WHERE 1=1 "
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
        sql.append("ORDER BY (p.status = 'IN_AUCTION') DESC, p.created_at DESC");

        List<ProduceLot> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapLotFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error browsing produce lots", e);
            throw new FairBidException("Database error loading produce lots", e, 500);
        }
        return list;
    }

    public void updateStatus(int lotId, LotStatus status) {
        String sql = "UPDATE produce_lots SET status = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, lotId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating lot status: " + lotId, e);
            throw new FairBidException("Database error updating lot status", e, 500);
        }
    }

    public void updateStatus(Connection conn, int lotId, LotStatus status) throws SQLException {
        String sql = "UPDATE produce_lots SET status = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, lotId);
            stmt.executeUpdate();
        }
    }

    public void updateMinPrice(int lotId, BigDecimal newPrice) {
        String sql = "UPDATE produce_lots SET min_price_per_unit = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, newPrice);
            stmt.setInt(2, lotId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating lot minimum price: " + lotId, e);
            throw new FairBidException("Database error updating minimum price", e, 500);
        }
    }

    public BigDecimal getFarmerTotalEarnings(int farmerId) {
        String sql = "SELECT COALESCE(SUM(ar.total_amount), 0) FROM auction_results ar " +
                "JOIN auctions a ON ar.auction_id = a.id " +
                "JOIN produce_lots p ON a.lot_id = p.id " +
                "WHERE p.farmer_id = ? AND ar.outcome = 'SOLD'";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, farmerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error calculating farmer earnings", e);
        }
        return BigDecimal.ZERO;
    }

    private ProduceLot mapLotFromResultSet(ResultSet rs) throws SQLException {
        ProduceLot lot = new ProduceLot(
                rs.getInt("id"),
                rs.getInt("farmer_id"),
                rs.getString("crop_name"),
                rs.getString("category"),
                rs.getBigDecimal("quantity"),
                rs.getString("unit"),
                rs.getBigDecimal("min_price_per_unit"),
                rs.getBigDecimal("agent_offer_per_unit"),
                LotStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("created_at")
        );
        lot.setFarmerName(rs.getString("farmer_name"));
        lot.setFarmerLocation(rs.getString("farmer_location"));
        int activeAuctionId = rs.getInt("active_auction_id");
        if (!rs.wasNull()) {
            lot.setActiveAuctionId(activeAuctionId);
        }
        lot.setCurrentHighestBid(rs.getBigDecimal("highest_bid"));
        return lot;
    }
}

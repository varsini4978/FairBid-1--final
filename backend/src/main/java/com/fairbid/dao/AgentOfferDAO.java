package com.fairbid.dao;

import com.fairbid.exception.FairBidException;
import com.fairbid.model.AgentDecision;
import com.fairbid.model.AgentOffer;

import java.math.BigDecimal;
import java.sql.*;
import java.util.logging.Level;

/**
 * Data Access Object for local agent offline offers and farmer decisions.
 */
public class AgentOfferDAO extends BaseDAO {

    public AgentOffer createOrUpdate(AgentOffer offer) {
        String checkSql = "SELECT id FROM agent_offers WHERE lot_id = ? LIMIT 1";
        String insertSql = "INSERT INTO agent_offers (lot_id, offered_price_per_unit, farmer_decision, transport_cost, other_expenses, decided_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        String updateSql = "UPDATE agent_offers SET offered_price_per_unit = ?, farmer_decision = ?, transport_cost = ?, other_expenses = ?, decided_at = ? " +
                "WHERE lot_id = ?";

        try (Connection conn = getConnection()) {
            boolean exists = false;
            int existingId = 0;
            try (PreparedStatement stmt = conn.prepareStatement(checkSql)) {
                stmt.setInt(1, offer.getLotId());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        exists = true;
                        existingId = rs.getInt("id");
                    }
                }
            }

            if (exists) {
                try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    stmt.setBigDecimal(1, offer.getOfferedPricePerUnit());
                    stmt.setString(2, offer.getFarmerDecision().name());
                    stmt.setBigDecimal(3, offer.getTransportCost());
                    stmt.setBigDecimal(4, offer.getOtherExpenses());
                    stmt.setTimestamp(5, offer.getDecidedAt());
                    stmt.setInt(6, offer.getLotId());
                    stmt.executeUpdate();
                    offer.setId(existingId);
                }
            } else {
                try (PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setInt(1, offer.getLotId());
                    stmt.setBigDecimal(2, offer.getOfferedPricePerUnit());
                    stmt.setString(3, offer.getFarmerDecision().name());
                    stmt.setBigDecimal(4, offer.getTransportCost());
                    stmt.setBigDecimal(5, offer.getOtherExpenses());
                    stmt.setTimestamp(6, offer.getDecidedAt());
                    stmt.executeUpdate();

                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            offer.setId(rs.getInt(1));
                        }
                    }
                }
            }
            return offer;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating/updating agent offer for lot: " + offer.getLotId(), e);
            throw new FairBidException("Database error saving agent offer", e, 500);
        }
    }

    public AgentOffer findByLotId(int lotId) {
        String sql = "SELECT ao.*, p.crop_name, p.quantity, p.unit " +
                "FROM agent_offers ao " +
                "JOIN produce_lots p ON ao.lot_id = p.id " +
                "WHERE ao.lot_id = ? " +
                "ORDER BY ao.id DESC LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, lotId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    AgentOffer offer = new AgentOffer(
                            rs.getInt("id"),
                            rs.getInt("lot_id"),
                            rs.getBigDecimal("offered_price_per_unit"),
                            AgentDecision.valueOf(rs.getString("farmer_decision")),
                            rs.getBigDecimal("transport_cost"),
                            rs.getBigDecimal("other_expenses"),
                            rs.getTimestamp("decided_at")
                    );
                    offer.setCropName(rs.getString("crop_name"));
                    offer.setQuantity(rs.getBigDecimal("quantity"));
                    offer.setUnit(rs.getString("unit"));
                    return offer;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding agent offer for lot: " + lotId, e);
            throw new FairBidException("Database error loading agent offer", e, 500);
        }
        return null;
    }

    public void recordDecision(Connection conn, int lotId, AgentDecision decision, BigDecimal transport, BigDecimal otherExp) throws SQLException {
        String sql = "UPDATE agent_offers SET farmer_decision = ?, transport_cost = ?, other_expenses = ?, decided_at = NOW() WHERE lot_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, decision.name());
            stmt.setBigDecimal(2, transport != null ? transport : BigDecimal.ZERO);
            stmt.setBigDecimal(3, otherExp != null ? otherExp : BigDecimal.ZERO);
            stmt.setInt(4, lotId);
            int updated = stmt.executeUpdate();
            if (updated == 0) {
                // If row didn't exist, create it
                String insertSql = "INSERT INTO agent_offers (lot_id, offered_price_per_unit, farmer_decision, transport_cost, other_expenses, decided_at) " +
                        "SELECT id, agent_offer_per_unit, ?, ?, ?, NOW() FROM produce_lots WHERE id = ?";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, decision.name());
                    insertStmt.setBigDecimal(2, transport != null ? transport : BigDecimal.ZERO);
                    insertStmt.setBigDecimal(3, otherExp != null ? otherExp : BigDecimal.ZERO);
                    insertStmt.setInt(4, lotId);
                    insertStmt.executeUpdate();
                }
            }
        }
    }
}

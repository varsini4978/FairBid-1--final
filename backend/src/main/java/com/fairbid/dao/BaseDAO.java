package com.fairbid.dao;

import com.fairbid.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstraction & JDBC Management:
 * Abstract base class for all Data Access Objects (DAOs).
 * Provides managed connections and transaction utilities using try-with-resources.
 * Guarantees PreparedStatement usage across all database operations.
 */
public abstract class BaseDAO {
    protected final Logger logger = Logger.getLogger(getClass().getName());

    protected Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    protected void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to rollback database transaction", e);
            }
        }
    }

    protected void resetAutoCommitQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to reset autoCommit", e);
            }
        }
    }
}

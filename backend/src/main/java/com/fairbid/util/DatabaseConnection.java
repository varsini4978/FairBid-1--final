package com.fairbid.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton database connection manager utilizing HikariCP high-performance connection pool.
 * Loads configuration from db.properties on the classpath.
 */
public class DatabaseConnection {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static HikariDataSource dataSource;

    static {
        try {
            Properties props = new Properties();
            try (InputStream in = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (in == null) {
                    throw new RuntimeException("db.properties not found on classpath");
                }
                props.load(in);
            }

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.user"));
            config.setPassword(props.getProperty("db.password"));
            config.setDriverClassName(props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));

            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("pool.maximumPoolSize", "10")));
            config.setMinimumIdle(Integer.parseInt(props.getProperty("pool.minimumIdle", "2")));
            config.setIdleTimeout(Long.parseLong(props.getProperty("pool.idleTimeout", "30000")));
            config.setConnectionTimeout(Long.parseLong(props.getProperty("pool.connectionTimeout", "10000")));
            config.setMaxLifetime(Long.parseLong(props.getProperty("pool.maxLifetime", "1800000")));

            // Standard MySQL performance settings
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");

            dataSource = new HikariDataSource(config);
            LOGGER.info("HikariCP connection pool successfully initialized for FairBid.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database connection pool", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    private DatabaseConnection() {
        // Prevent instantiation
    }

    /**
     * Obtains an active database connection from the pool.
     *
     * @return Connection instance
     * @throws SQLException on database connection error
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Gracefully shuts down the connection pool upon servlet context destruction.
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            LOGGER.info("HikariCP connection pool closed.");
        }
    }
}

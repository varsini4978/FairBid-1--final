package com.fairbid.listener;

import com.fairbid.service.AuctionManager;
import com.fairbid.util.DatabaseConnection;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet Context Lifecycle Listener:
 * Automatically starts a background ScheduledExecutorService checking for expired
 * OPEN auctions every 5 seconds and triggering idempotent winner settlement.
 * Gracefully terminates the executor and database connection pool on context shutdown.
 */
@WebListener
public class AuctionSchedulerListener implements ServletContextListener {
    private static final Logger LOGGER = Logger.getLogger(AuctionSchedulerListener.class.getName());
    private ScheduledExecutorService scheduler;
    private final AuctionManager auctionManager = new AuctionManager();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("FairBid context initialized. Starting Auction Auto-Closing Scheduler...");
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "FairBid-AuctionScheduler-Thread");
            t.setDaemon(true);
            return t;
        });

        // Run auto-close check every 5 seconds
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                auctionManager.closeAllExpiredAuctions();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error occurred during scheduled auction closing cycle", e);
            }
        }, 5, 5, TimeUnit.SECONDS);

        LOGGER.info("Auction Auto-Closing Scheduler running (polling every 5 seconds).");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("FairBid context destroying. Shutting down Scheduler and Database Connection Pool...");
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(3, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        DatabaseConnection.shutdown();
        LOGGER.info("FairBid background services shut down cleanly.");
    }
}

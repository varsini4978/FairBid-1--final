-- ============================================================================
-- FairBid – Farmer Produce Auction System
-- Database Schema & Comprehensive Sample Data
-- Target Database: MySQL 8.0 (InnoDB, utf8mb4)
-- ============================================================================

CREATE DATABASE IF NOT EXISTS `fairbid_db` 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE `fairbid_db`;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `auction_results`;
DROP TABLE IF EXISTS `bids`;
DROP TABLE IF EXISTS `auctions`;
DROP TABLE IF EXISTS `agent_offers`;
DROP TABLE IF EXISTS `produce_lots`;
DROP TABLE IF EXISTS `buyer_profiles`;
DROP TABLE IF EXISTS `farmer_profiles`;
DROP TABLE IF EXISTS `users`;

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------------------------------------------------------
-- 1. Table: users
-- Core entity for authentication and inheritance hierarchy (Farmer & Buyer)
-- ----------------------------------------------------------------------------
CREATE TABLE `users` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `role` ENUM('FARMER', 'BUYER') NOT NULL,
    `full_name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `phone` VARCHAR(20) NOT NULL,
    `location` VARCHAR(100) NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_user_email` (`email`),
    INDEX `idx_user_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 2. Table: farmer_profiles
-- Profile extension for Farmer subclass (User -> Farmer)
-- ----------------------------------------------------------------------------
CREATE TABLE `farmer_profiles` (
    `user_id` INT PRIMARY KEY,
    `farm_name` VARCHAR(100) NOT NULL,
    `village` VARCHAR(100) NOT NULL,
    `district` VARCHAR(100) NOT NULL,
    CONSTRAINT `fk_farmer_user` FOREIGN KEY (`user_id`) 
        REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 3. Table: buyer_profiles
-- Profile extension for Buyer subclass (User -> Buyer)
-- ----------------------------------------------------------------------------
CREATE TABLE `buyer_profiles` (
    `user_id` INT PRIMARY KEY,
    `business_name` VARCHAR(100) NOT NULL,
    `buyer_type` VARCHAR(50) NOT NULL,
    CONSTRAINT `fk_buyer_user` FOREIGN KEY (`user_id`) 
        REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 4. Table: produce_lots
-- Agricultural lots listed by farmers
-- ----------------------------------------------------------------------------
CREATE TABLE `produce_lots` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `farmer_id` INT NOT NULL,
    `crop_name` VARCHAR(100) NOT NULL,
    `category` VARCHAR(50) NOT NULL,
    `quantity` DECIMAL(10,2) NOT NULL,
    `unit` VARCHAR(20) NOT NULL,
    `min_price_per_unit` DECIMAL(10,2) NOT NULL,
    `agent_offer_per_unit` DECIMAL(10,2) NOT NULL,
    `status` ENUM('AVAILABLE', 'IN_AUCTION', 'SOLD', 'UNSOLD', 'SOLD_TO_AGENT') NOT NULL DEFAULT 'AVAILABLE',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_lot_farmer` FOREIGN KEY (`farmer_id`) 
        REFERENCES `users`(`id`) ON DELETE CASCADE,
    CONSTRAINT `chk_lot_quantity` CHECK (`quantity` > 0),
    CONSTRAINT `chk_lot_min_price` CHECK (`min_price_per_unit` > 0),
    CONSTRAINT `chk_lot_agent_offer` CHECK (`agent_offer_per_unit` >= 0),
    INDEX `idx_lot_status` (`status`),
    INDEX `idx_lot_farmer` (`farmer_id`),
    INDEX `idx_lot_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 5. Table: auctions
-- Auction rounds for produce lots. Multiple rounds preserve round history.
-- ----------------------------------------------------------------------------
CREATE TABLE `auctions` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `lot_id` INT NOT NULL,
    `round_number` INT NOT NULL DEFAULT 1,
    `min_price_snapshot` DECIMAL(10,2) NOT NULL,
    `start_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `end_time` TIMESTAMP NOT NULL,
    `status` ENUM('OPEN', 'CLOSED') NOT NULL DEFAULT 'OPEN',
    `outcome` ENUM('SOLD', 'UNSOLD') NULL,
    UNIQUE KEY `uk_lot_round` (`lot_id`, `round_number`),
    CONSTRAINT `fk_auction_lot` FOREIGN KEY (`lot_id`) 
        REFERENCES `produce_lots`(`id`) ON DELETE CASCADE,
    INDEX `idx_auction_status_time` (`status`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 6. Table: bids
-- Competitive bids submitted by buyers during open auction rounds
-- ----------------------------------------------------------------------------
CREATE TABLE `bids` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `auction_id` INT NOT NULL,
    `buyer_id` INT NOT NULL,
    `amount_per_unit` DECIMAL(10,2) NOT NULL,
    `placed_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `status` ENUM('VALID', 'WINNING', 'OUTBID', 'REJECTED') NOT NULL DEFAULT 'VALID',
    CONSTRAINT `fk_bid_auction` FOREIGN KEY (`auction_id`) 
        REFERENCES `auctions`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_bid_buyer` FOREIGN KEY (`buyer_id`) 
        REFERENCES `users`(`id`) ON DELETE CASCADE,
    CONSTRAINT `chk_bid_amount` CHECK (`amount_per_unit` > 0),
    INDEX `idx_auction_bids_ordering` (`auction_id`, `amount_per_unit` DESC, `placed_at` ASC),
    INDEX `idx_buyer_bids` (`buyer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 7. Table: auction_results
-- Settled results for closed auctions
-- ----------------------------------------------------------------------------
CREATE TABLE `auction_results` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `auction_id` INT NOT NULL UNIQUE,
    `winning_bid_id` INT NULL,
    `winner_buyer_id` INT NULL,
    `final_price_per_unit` DECIMAL(10,2) NULL,
    `total_amount` DECIMAL(10,2) NULL,
    `outcome` ENUM('SOLD', 'UNSOLD') NOT NULL,
    `closed_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_result_auction` FOREIGN KEY (`auction_id`) 
        REFERENCES `auctions`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_result_bid` FOREIGN KEY (`winning_bid_id`) 
        REFERENCES `bids`(`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_result_buyer` FOREIGN KEY (`winner_buyer_id`) 
        REFERENCES `users`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 8. Table: agent_offers
-- External agent offer records and decision tracking for unsold recovery
-- ----------------------------------------------------------------------------
CREATE TABLE `agent_offers` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `lot_id` INT NOT NULL,
    `offered_price_per_unit` DECIMAL(10,2) NOT NULL,
    `farmer_decision` ENUM('PENDING', 'ACCEPTED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    `transport_cost` DECIMAL(10,2) DEFAULT 0.00,
    `other_expenses` DECIMAL(10,2) DEFAULT 0.00,
    `decided_at` TIMESTAMP NULL,
    CONSTRAINT `fk_agent_lot` FOREIGN KEY (`lot_id`) 
        REFERENCES `produce_lots`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================================
-- SAMPLE DATA INSERTIONS
-- Verified BCrypt Hashes:
-- Farmer@123 -> $2a$12$olUQo7C7Bt9Y7yMQ7KWHeeGFhRmbDEMYSTB6d3tRUOG.LE3Nc8AFi
-- Buyer@123  -> $2a$12$iJg194/J3xOw2wYzWMJ45.1.QfNdPdH4nyHs1ncf./4TCPtEmSxU.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Users: 3 Farmers & 4 Buyers
-- ----------------------------------------------------------------------------
INSERT INTO `users` (`id`, `role`, `full_name`, `email`, `password_hash`, `phone`, `location`, `created_at`) VALUES
(1, 'FARMER', 'Ramesh Kumar', 'farmer1@fairbid.com', '$2a$12$olUQo7C7Bt9Y7yMQ7KWHeeGFhRmbDEMYSTB6d3tRUOG.LE3Nc8AFi', '9876543210', 'Thanjavur, Tamil Nadu', NOW() - INTERVAL 10 DAY),
(2, 'FARMER', 'Suresh Patel', 'farmer2@fairbid.com', '$2a$12$olUQo7C7Bt9Y7yMQ7KWHeeGFhRmbDEMYSTB6d3tRUOG.LE3Nc8AFi', '9876543211', 'Anand, Gujarat', NOW() - INTERVAL 8 DAY),
(3, 'FARMER', 'Rajesh Sharma', 'farmer3@fairbid.com', '$2a$12$olUQo7C7Bt9Y7yMQ7KWHeeGFhRmbDEMYSTB6d3tRUOG.LE3Nc8AFi', '9876543212', 'Karnal, Haryana', NOW() - INTERVAL 7 DAY),

(4, 'BUYER', 'Priya Agro Traders', 'buyer1@fairbid.com', '$2a$12$iJg194/J3xOw2wYzWMJ45.1.QfNdPdH4nyHs1ncf./4TCPtEmSxU.', '9123456780', 'Chennai, Tamil Nadu', NOW() - INTERVAL 9 DAY),
(5, 'BUYER', 'Apex Food Processors', 'buyer2@fairbid.com', '$2a$12$iJg194/J3xOw2wYzWMJ45.1.QfNdPdH4nyHs1ncf./4TCPtEmSxU.', '9123456781', 'Ahmedabad, Gujarat', NOW() - INTERVAL 8 DAY),
(6, 'BUYER', 'Organic Fresh Markets', 'buyer3@fairbid.com', '$2a$12$iJg194/J3xOw2wYzWMJ45.1.QfNdPdH4nyHs1ncf./4TCPtEmSxU.', '9123456782', 'Bengaluru, Karnataka', NOW() - INTERVAL 6 DAY),
(7, 'BUYER', 'Global Agri Export Corp', 'buyer4@fairbid.com', '$2a$12$iJg194/J3xOw2wYzWMJ45.1.QfNdPdH4nyHs1ncf./4TCPtEmSxU.', '9123456783', 'Mumbai, Maharashtra', NOW() - INTERVAL 5 DAY);

-- Farmer Profiles
INSERT INTO `farmer_profiles` (`user_id`, `farm_name`, `village`, `district`) VALUES
(1, 'Green Valley Organics', 'Papanasam', 'Thanjavur'),
(2, 'Patel Agri Farms', 'Boriyavi', 'Anand'),
(3, 'Golden Harvest Fields', 'Taraori', 'Karnal');

-- Buyer Profiles
INSERT INTO `buyer_profiles` (`user_id`, `business_name`, `buyer_type`) VALUES
(4, 'Priya Agro Traders Pvt Ltd', 'Wholesale Distributor'),
(5, 'Apex Food Processors India', 'Food Processor'),
(6, 'Organic Fresh Hypermarkets', 'Retail Chain'),
(7, 'Global Agri Export Corporation', 'International Exporter');

-- ----------------------------------------------------------------------------
-- Produce Lots (8+ diverse Indian crops across 5 categories)
-- ----------------------------------------------------------------------------
INSERT INTO `produce_lots` (`id`, `farmer_id`, `crop_name`, `category`, `quantity`, `unit`, `min_price_per_unit`, `agent_offer_per_unit`, `status`, `created_at`) VALUES
-- Lot 1: Worked Example - Tomatoes, 100 kg, Min ₹25/kg, Local Agent ₹22/kg (Active Auction)
(1, 1, 'Organic Hybrid Tomatoes', 'Vegetables', 100.00, 'kg', 25.00, 22.00, 'IN_AUCTION', NOW() - INTERVAL 2 DAY),
-- Lot 2: Alphonso Mangoes (Active Auction)
(2, 2, 'Ratnagiri Alphonso Mangoes', 'Fruits', 50.00, 'crate', 1200.00, 1050.00, 'IN_AUCTION', NOW() - INTERVAL 2 DAY),
-- Lot 3: Basmati Rice (Closed and SOLD with winner)
(3, 3, 'Traditional 1121 Basmati Rice', 'Grains', 20.00, 'quintal', 4500.00, 4100.00, 'SOLD', NOW() - INTERVAL 4 DAY),
-- Lot 4: Red Onions (Closed and UNSOLD, Agent offer pending comparison)
(4, 2, 'Nasik Red Onions', 'Vegetables', 80.00, 'quintal', 1800.00, 1600.00, 'UNSOLD', NOW() - INTERVAL 3 DAY),
-- Lot 5: Malabar Cardamom (Multi-round: Round 1 UNSOLD, Round 2 OPEN)
(5, 1, 'Grade A Malabar Cardamom', 'Spices', 30.00, 'kg', 1650.00, 1500.00, 'IN_AUCTION', NOW() - INTERVAL 5 DAY),
-- Lot 6: Green Gram / Moong Dal (AVAILABLE - ready to be auctioned)
(6, 3, 'Polished Green Gram (Moong Dal)', 'Pulses', 15.00, 'quintal', 7200.00, 6800.00, 'AVAILABLE', NOW() - INTERVAL 1 DAY),
-- Lot 7: Cold Storage Potatoes (Sold directly to agent after unsold review)
(7, 1, 'Agra Jyoti Table Potatoes', 'Vegetables', 500.00, 'kg', 18.00, 16.00, 'SOLD_TO_AGENT', NOW() - INTERVAL 6 DAY),
-- Lot 8: Kashmiri Apples (AVAILABLE)
(8, 3, 'Kashmiri Delicious Apples', 'Fruits', 40.00, 'crate', 2200.00, 1950.00, 'AVAILABLE', NOW() - INTERVAL 12 HOUR),
-- Lot 9: Sharbati Wheat (Active Auction)
(9, 2, 'MP Sharbati Gold Wheat', 'Grains', 25.00, 'quintal', 3100.00, 2850.00, 'IN_AUCTION', NOW() - INTERVAL 1 DAY);

-- ----------------------------------------------------------------------------
-- Auctions (Multi-round history demonstrated on Lot 5)
-- ----------------------------------------------------------------------------
INSERT INTO `auctions` (`id`, `lot_id`, `round_number`, `min_price_snapshot`, `start_time`, `end_time`, `status`, `outcome`) VALUES
-- Auction 1: Lot 1 (Tomatoes) - OPEN, ends in 24 hours
(1, 1, 1, 25.00, NOW() - INTERVAL 12 HOUR, NOW() + INTERVAL 24 HOUR, 'OPEN', NULL),
-- Auction 2: Lot 2 (Alphonso Mangoes) - OPEN, ends in 18 hours
(2, 2, 1, 1200.00, NOW() - INTERVAL 6 HOUR, NOW() + INTERVAL 18 HOUR, 'OPEN', NULL),
-- Auction 3: Lot 3 (Basmati Rice) - CLOSED & SOLD
(3, 3, 1, 4500.00, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 1 DAY, 'CLOSED', 'SOLD'),
-- Auction 4: Lot 4 (Red Onions) - CLOSED & UNSOLD (no valid bids)
(4, 4, 1, 1800.00, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 6 HOUR, 'CLOSED', 'UNSOLD'),
-- Auction 5: Lot 5 (Cardamom) Round 1 - CLOSED & UNSOLD
(5, 5, 1, 1800.00, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 2 DAY, 'CLOSED', 'UNSOLD'),
-- Auction 6: Lot 5 (Cardamom) Round 2 - OPEN (revised min price from 1800 to 1650)
(6, 5, 2, 1650.00, NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 30 HOUR, 'OPEN', NULL),
-- Auction 7: Lot 9 (Sharbati Wheat) - OPEN, ends in 36 hours
(7, 9, 1, 3100.00, NOW() - INTERVAL 8 HOUR, NOW() + INTERVAL 36 HOUR, 'OPEN', NULL);

-- ----------------------------------------------------------------------------
-- Bids (Ordered bids, outbid logic, winning bids)
-- ----------------------------------------------------------------------------
INSERT INTO `bids` (`id`, `auction_id`, `buyer_id`, `amount_per_unit`, `placed_at`, `status`) VALUES
-- Bids on Auction 1 (Tomatoes: Min ₹25/kg)
(1, 1, 4, 26.00, NOW() - INTERVAL 10 HOUR, 'OUTBID'),
(2, 1, 6, 27.50, NOW() - INTERVAL 8 HOUR, 'OUTBID'),
(3, 1, 4, 29.00, NOW() - INTERVAL 4 HOUR, 'WINNING'),

-- Bids on Auction 2 (Mangoes: Min ₹1200/crate)
(4, 2, 5, 1250.00, NOW() - INTERVAL 5 HOUR, 'OUTBID'),
(5, 2, 7, 1320.00, NOW() - INTERVAL 2 HOUR, 'WINNING'),

-- Bids on Auction 3 (Basmati Rice: Min ₹4500/quintal) - Settled Winner
(6, 3, 4, 4600.00, NOW() - INTERVAL 2 DAY - INTERVAL 12 HOUR, 'OUTBID'),
(7, 3, 5, 4750.00, NOW() - INTERVAL 2 DAY - INTERVAL 4 HOUR, 'OUTBID'),
(8, 3, 7, 4850.00, NOW() - INTERVAL 1 DAY - INTERVAL 2 HOUR, 'WINNING'),

-- Bids on Auction 6 (Cardamom Round 2: Revised Min ₹1650/kg)
(9, 6, 6, 1700.00, NOW() - INTERVAL 18 HOUR, 'OUTBID'),
(10, 6, 7, 1750.00, NOW() - INTERVAL 6 HOUR, 'WINNING'),

-- Bids on Auction 7 (Wheat: Min ₹3100/quintal)
(11, 7, 4, 3150.00, NOW() - INTERVAL 6 HOUR, 'OUTBID'),
(12, 7, 5, 3220.00, NOW() - INTERVAL 1 HOUR, 'WINNING');

-- ----------------------------------------------------------------------------
-- Auction Results
-- ----------------------------------------------------------------------------
INSERT INTO `auction_results` (`id`, `auction_id`, `winning_bid_id`, `winner_buyer_id`, `final_price_per_unit`, `total_amount`, `outcome`, `closed_at`) VALUES
-- Settled Auction 3: Winner buyer 7 (Global Agri Export Corp) at ₹4850/quintal for 20 quintals = ₹97,000
(1, 3, 8, 7, 4850.00, 97000.00, 'SOLD', NOW() - INTERVAL 1 DAY),
-- Settled Auction 4: Lot 4 UNSOLD (no bids placed above minimum)
(2, 4, NULL, NULL, NULL, NULL, 'UNSOLD', NOW() - INTERVAL 6 HOUR),
-- Settled Auction 5: Lot 5 Round 1 UNSOLD (prompting farmer to revise price in Round 2)
(3, 5, NULL, NULL, NULL, NULL, 'UNSOLD', NOW() - INTERVAL 2 DAY);

-- ----------------------------------------------------------------------------
-- Agent Offers & Decisions
-- ----------------------------------------------------------------------------
INSERT INTO `agent_offers` (`id`, `lot_id`, `offered_price_per_unit`, `farmer_decision`, `transport_cost`, `other_expenses`, `decided_at`) VALUES
-- Agent offer for Lot 1 (Tomatoes): ₹22/kg (Active comparison reference)
(1, 1, 22.00, 'PENDING', 0.00, 0.00, NULL),
-- Agent offer for Lot 4 (Unsold Onions): ₹1600/quintal (Pending farmer decision)
(2, 4, 1600.00, 'PENDING', 300.00, 150.00, NULL),
-- Agent offer for Lot 5 (Cardamom): ₹1500/kg (Rejected by farmer in favor of Round 2)
(3, 5, 1500.00, 'REJECTED', 200.00, 50.00, NOW() - INTERVAL 2 DAY),
-- Agent offer for Lot 7 (Potatoes): ₹16/kg (Accepted by farmer with transport expenses)
(4, 7, 16.00, 'ACCEPTED', 400.00, 100.00, NOW() - INTERVAL 5 DAY);

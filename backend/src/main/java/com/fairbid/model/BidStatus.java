package com.fairbid.model;

/**
 * Status of a bid submitted in an auction.
 */
public enum BidStatus {
    VALID,      // Validly submitted, currently evaluated
    WINNING,    // Highest active bid (or won the auction upon closing)
    OUTBID,     // Superseded by a subsequent higher valid bid
    REJECTED    // Rejected by business rule validation
}

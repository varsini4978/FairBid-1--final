package com.fairbid.model;

/**
 * Lifecycle states of an agricultural produce lot listed by a farmer.
 */
public enum LotStatus {
    AVAILABLE,       // Listed by farmer, ready for an auction to start
    IN_AUCTION,      // Currently linked to an OPEN auction round
    SOLD,            // Successfully sold to the highest bidder
    UNSOLD,          // Auction closed with no valid bids (reopening or agent review possible)
    SOLD_TO_AGENT    // Farmer accepted the local agent's offline offer
}

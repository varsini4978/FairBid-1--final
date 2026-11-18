package com.fairbid.exception;

/**
 * Thrown when an action (such as submitting a bid) is attempted on an auction that has already closed.
 * Maps to HTTP 409 Conflict.
 */
public class AuctionClosedException extends FairBidException {
    public AuctionClosedException(String message) {
        super(message, 409);
    }
}

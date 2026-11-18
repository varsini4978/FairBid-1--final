package com.fairbid.exception;

/**
 * Thrown when a submitted bid violates pricing rules (e.g., bid below minimum price,
 * or bid not strictly greater than current highest valid bid).
 * Maps to HTTP 400 Bad Request or 409 Conflict.
 */
public class InvalidBidException extends FairBidException {
    public InvalidBidException(String message) {
        super(message, 400);
    }

    public InvalidBidException(String message, int statusCode) {
        super(message, statusCode);
    }
}

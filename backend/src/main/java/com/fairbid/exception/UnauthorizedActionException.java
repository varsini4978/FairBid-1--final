package com.fairbid.exception;

/**
 * Thrown when an authenticated user attempts an operation outside their allowed role or ownership.
 * (e.g., Farmer attempting to bid, Buyer trying to close auction, or editing another user's lot).
 * Maps to HTTP 403 Forbidden.
 */
public class UnauthorizedActionException extends FairBidException {
    public UnauthorizedActionException(String message) {
        super(message, 403);
    }
}

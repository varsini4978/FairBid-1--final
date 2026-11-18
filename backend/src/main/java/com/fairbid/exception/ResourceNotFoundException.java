package com.fairbid.exception;

/**
 * Thrown when a requested resource (lot, auction, user) does not exist in the database.
 * Maps to HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends FairBidException {
    public ResourceNotFoundException(String message) {
        super(message, 404);
    }
}

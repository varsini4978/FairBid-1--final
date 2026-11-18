package com.fairbid.exception;

/**
 * Base unchecked exception for the FairBid application.
 * All domain-specific business exceptions inherit from this class.
 */
public class FairBidException extends RuntimeException {
    private final int statusCode;

    public FairBidException(String message) {
        this(message, 400);
    }

    public FairBidException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public FairBidException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}

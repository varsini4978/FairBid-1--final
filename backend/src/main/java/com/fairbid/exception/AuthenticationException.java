package com.fairbid.exception;

/**
 * Thrown when credentials are invalid or session is missing/expired.
 * Maps to HTTP 401 Unauthorized.
 */
public class AuthenticationException extends FairBidException {
    public AuthenticationException(String message) {
        super(message, 401);
    }
}

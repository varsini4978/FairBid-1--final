package com.fairbid.exception;

/**
 * Thrown during registration when an email is already taken by another account.
 * Maps to HTTP 409 Conflict.
 */
public class DuplicateUserException extends FairBidException {
    public DuplicateUserException(String message) {
        super(message, 409);
    }
}

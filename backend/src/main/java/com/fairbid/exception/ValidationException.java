package com.fairbid.exception;

import java.util.Collections;
import java.util.Map;

/**
 * Thrown when client input fails business validation rules (e.g., negative prices, malformed emails).
 * Maps to HTTP 400 Bad Request.
 */
public class ValidationException extends FairBidException {
    private final Map<String, String> fieldErrors;

    public ValidationException(String message) {
        super(message, 400);
        this.fieldErrors = Collections.emptyMap();
    }

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message, 400);
        this.fieldErrors = fieldErrors != null ? fieldErrors : Collections.emptyMap();
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}

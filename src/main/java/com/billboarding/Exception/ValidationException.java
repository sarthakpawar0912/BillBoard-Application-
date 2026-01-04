package com.billboarding.Exception;

import org.springframework.http.HttpStatus;

/**
 * Exception for validation errors.
 * Returns HTTP 422 Unprocessable Entity.
 */
public class ValidationException extends BusinessException {

    public ValidationException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_ERROR");
    }

    public ValidationException(String field, String message) {
        super("Validation failed for '" + field + "': " + message, HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_ERROR");
    }
}

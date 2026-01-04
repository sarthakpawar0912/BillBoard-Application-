package com.billboarding.Exception;

import org.springframework.http.HttpStatus;

/**
 * Exception for forbidden access (authenticated but not authorized).
 * Returns HTTP 403 Forbidden.
 */
public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN, "FORBIDDEN");
    }

    public ForbiddenException() {
        super("Access denied", HttpStatus.FORBIDDEN, "FORBIDDEN");
    }
}

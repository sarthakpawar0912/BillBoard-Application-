package com.billboarding.Exception;

import org.springframework.http.HttpStatus;

/**
 * Exception for unauthorized access attempts.
 * Returns HTTP 401 Unauthorized.
 */
public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }

    public UnauthorizedException() {
        super("Unauthorized access", HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }
}

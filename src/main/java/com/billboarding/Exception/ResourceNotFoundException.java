package com.billboarding.Exception;

import org.springframework.http.HttpStatus;

/**
 * Exception for when a requested resource is not found.
 * Returns HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " not found with id: " + id, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }

    public ResourceNotFoundException(String resource, String identifier) {
        super(resource + " not found: " + identifier, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }
}

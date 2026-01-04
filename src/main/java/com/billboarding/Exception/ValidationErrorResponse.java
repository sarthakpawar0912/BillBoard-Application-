package com.billboarding.Exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Response object for validation errors with field-level details.
 * Returns HTTP 400 with specific field errors for form validation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorResponse {

    private int status;
    private String message;
    private String path;
    private Map<String, String> fieldErrors;
}

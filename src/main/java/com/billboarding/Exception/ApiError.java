package com.billboarding.Exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    private int status;
    private String message;
    private String path;
    private String errorCode;
    private LocalDateTime timestamp;
    private Map<String, String> fieldErrors;

    public ApiError(int status, String message, String path) {
        this.status = status;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }

    public ApiError(int status, String message, String path, String errorCode) {
        this.status = status;
        this.message = message;
        this.path = path;
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }
}
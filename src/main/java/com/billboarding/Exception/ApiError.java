package com.billboarding.Exception;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {

    private int status;     // HTTP Status code
    private String message; // Human-readable message
    private String path;    // API endpoint path
}
package com.billboarding.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response returned after successful login
 */
@Data
@AllArgsConstructor
public class AuthResponse {


    private boolean twoFactorRequired;
    private String token;   // JWT token
    private String role;    // User role (ADMIN / OWNER / ADVERTISER)
    private Long userId;    // Logged-in user id
    private String message; // Optional message
}
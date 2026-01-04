package com.billboarding.DTO.ADMIN;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for Admin Owners list with aggregated stats.
 * Returns owner user data along with billboard count and earnings.
 */
@Data
@Builder
public class OwnerStatsResponse {

    // User fields
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String kycStatus;
    private boolean blocked;
    private String createdAt;

    // Aggregated stats
    private int billboardCount;
    private double totalEarnings;

    // Optional: Company name from owner profile
    private String company;
}

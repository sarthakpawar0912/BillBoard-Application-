package com.billboarding.DTO.ADMIN.Wallet;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyCommissionDTO {

    private String month;      // e.g., "2025-01"
    private String monthName;  // e.g., "January 2025"
    private Double amount;
    private Long transactionCount;
}

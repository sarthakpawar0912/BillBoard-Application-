package com.billboarding.DTO.OWNER;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OwnerMonthlyRevenueDTO {
    private String month;   // 2025-01
    private Double revenue;
}

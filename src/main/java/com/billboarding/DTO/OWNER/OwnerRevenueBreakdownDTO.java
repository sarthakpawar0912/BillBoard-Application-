package com.billboarding.DTO.OWNER;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OwnerRevenueBreakdownDTO {

    private Double grossAmount;
    private Double commission;
    private Double netPayout;
}

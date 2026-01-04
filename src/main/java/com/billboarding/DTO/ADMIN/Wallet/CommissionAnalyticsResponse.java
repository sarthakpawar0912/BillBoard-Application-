package com.billboarding.DTO.ADMIN.Wallet;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommissionAnalyticsResponse {

    private Double currentBalance;
    private Double totalEarned;
    private Double totalWithdrawn;
    private Double pendingWithdrawal;

    private Double todayEarnings;
    private Double weekEarnings;
    private Double monthEarnings;
    private Double yearEarnings;

    private Long totalTransactions;
    private Long totalCredits;
    private Long totalDebits;

    private Double averageCommissionPerBooking;
    private Double highestCommission;
    private Double lowestCommission;

    private List<MonthlyCommissionDTO> monthlyBreakdown;
}

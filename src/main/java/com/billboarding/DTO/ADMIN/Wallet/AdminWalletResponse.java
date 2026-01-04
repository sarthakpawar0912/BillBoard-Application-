package com.billboarding.DTO.ADMIN.Wallet;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminWalletResponse {

    private Double currentBalance;
    private Double totalEarned;
    private Double totalWithdrawn;
    private Double pendingWithdrawal;
    private Double availableForWithdrawal;
    private LocalDateTime updatedAt;
}

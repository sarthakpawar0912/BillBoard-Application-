package com.billboarding.DTO.ADMIN.Wallet;

import com.billboarding.Entity.ADMIN.wallet.AdminPayout;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminPayoutResponse {
    private Long id;
    private Double amount;
    private String status;
    private String bankAccountMasked;
    private String bankName;
    private String utrNumber;
    private String notes;
    private String failureReason;
    private LocalDateTime initiatedAt;
    private LocalDateTime processedAt;
    private LocalDateTime completedAt;

    public static AdminPayoutResponse fromEntity(AdminPayout payout) {
        String maskedAccount = null;
        String bankName = null;

        if (payout.getBankAccount() != null) {
            String accNum = payout.getBankAccount().getAccountNumber();
            if (accNum != null && accNum.length() >= 4) {
                maskedAccount = "X".repeat(accNum.length() - 4) + accNum.substring(accNum.length() - 4);
            }
            bankName = payout.getBankAccount().getBankName();
        }

        return AdminPayoutResponse.builder()
                .id(payout.getId())
                .amount(payout.getAmount())
                .status(payout.getStatus())
                .bankAccountMasked(maskedAccount)
                .bankName(bankName)
                .utrNumber(payout.getUtrNumber())
                .notes(payout.getNotes())
                .failureReason(payout.getFailureReason())
                .initiatedAt(payout.getInitiatedAt())
                .processedAt(payout.getProcessedAt())
                .completedAt(payout.getCompletedAt())
                .build();
    }
}

package com.billboarding.DTO.ADMIN.Wallet;

import com.billboarding.Entity.ADMIN.wallet.AdminBankAccount;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminBankAccountResponse {
    private Long id;
    private String accountHolderName;
    private String maskedAccountNumber;
    private String ifscCode;
    private String bankName;
    private String branchName;
    private String accountType;
    private Boolean isPrimary;
    private Boolean isVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AdminBankAccountResponse fromEntity(AdminBankAccount account) {
        return AdminBankAccountResponse.builder()
                .id(account.getId())
                .accountHolderName(account.getAccountHolderName())
                .maskedAccountNumber(maskAccountNumber(account.getAccountNumber()))
                .ifscCode(account.getIfscCode())
                .bankName(account.getBankName())
                .branchName(account.getBranchName())
                .accountType(account.getAccountType())
                .isPrimary(account.getIsPrimary())
                .isVerified(account.getIsVerified())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    private static String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return "****";
        }
        int length = accountNumber.length();
        String lastFour = accountNumber.substring(length - 4);
        return "X".repeat(length - 4) + lastFour;
    }
}

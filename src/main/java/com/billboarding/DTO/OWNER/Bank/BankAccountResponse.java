package com.billboarding.DTO.OWNER.Bank;

import com.billboarding.Entity.OWNER.bank.OwnerBankAccount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccountResponse {

    private Long id;
    private String accountHolderName;
    private String maskedAccountNumber;  // XXXX1234
    private String ifscCode;
    private String bankName;
    private String branchName;
    private String accountType;
    private String verificationStatus;
    private boolean readyForPayout;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BankAccountResponse fromEntity(OwnerBankAccount entity) {
        return BankAccountResponse.builder()
                .id(entity.getId())
                .accountHolderName(entity.getAccountHolderName())
                .maskedAccountNumber(entity.getMaskedAccountNumber())
                .ifscCode(entity.getIfscCode())
                .bankName(entity.getBankName())
                .branchName(entity.getBranchName())
                .accountType(entity.getAccountType())
                .verificationStatus(entity.getVerificationStatus())
                .readyForPayout(entity.isReadyForPayout())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

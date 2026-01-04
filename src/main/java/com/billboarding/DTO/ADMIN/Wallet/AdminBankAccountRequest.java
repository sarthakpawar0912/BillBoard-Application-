package com.billboarding.DTO.ADMIN.Wallet;

import lombok.Data;

@Data
public class AdminBankAccountRequest {
    private String accountHolderName;
    private String accountNumber;
    private String confirmAccountNumber;
    private String ifscCode;
    private String bankName;
    private String branchName;
    private String accountType; // SAVINGS, CURRENT
}

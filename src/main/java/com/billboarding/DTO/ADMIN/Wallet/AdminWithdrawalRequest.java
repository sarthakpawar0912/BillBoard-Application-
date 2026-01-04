package com.billboarding.DTO.ADMIN.Wallet;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminWithdrawalRequest {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;

    private String bankAccountId;  // Optional: for Razorpay fund account
    private String notes;
}

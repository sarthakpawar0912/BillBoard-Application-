package com.billboarding.DTO.Cancellation;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminCancellationStats {
    private long totalCancelled;
    private long cancelledNoRefund;
    private long cancelledBeforePayment;
    private double revenueRetained;
}

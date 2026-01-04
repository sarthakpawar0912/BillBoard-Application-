package com.billboarding.DTO.Payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * Request DTO for initiating a payment order.
 * Validates that booking ID is provided and positive.
 */
@Data
public class CreatePaymentOrderRequest {

    @NotNull(message = "Booking ID is required")
    @Positive(message = "Booking ID must be a positive number")
    private Long bookingId;
}
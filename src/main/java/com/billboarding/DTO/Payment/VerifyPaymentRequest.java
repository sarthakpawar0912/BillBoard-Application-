package com.billboarding.DTO.Payment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request DTO for verifying Razorpay payment signature.
 * All fields are mandatory for payment verification security.
 */
@Data
public class VerifyPaymentRequest {

    @NotBlank(message = "Razorpay Order ID is required")
    private String razorpayOrderId;

    @NotBlank(message = "Razorpay Payment ID is required")
    private String razorpayPaymentId;

    @NotBlank(message = "Razorpay Signature is required for payment verification")
    private String razorpaySignature;
}
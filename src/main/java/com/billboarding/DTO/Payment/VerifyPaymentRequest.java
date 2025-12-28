package com.billboarding.DTO.Payment;

import lombok.Data;

@Data
public class VerifyPaymentRequest {

    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
}
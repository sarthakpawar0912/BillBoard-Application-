package com.billboarding.DTO.Payment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentOrderResponse {

    private String orderId;      // Razorpay order id
    private String keyId;        // Razorpay publishable key
    private Long bookingId;
    private double amount;       // in rupees
    private String currency;     // INR
    private String receipt;      // BOOKING_<id>
}
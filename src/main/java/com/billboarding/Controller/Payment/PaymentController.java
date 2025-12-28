package com.billboarding.Controller.Payment;

import com.billboarding.DTO.Payment.CreatePaymentOrderRequest;
import com.billboarding.DTO.Payment.PaymentOrderResponse;
import com.billboarding.DTO.Payment.VerifyPaymentRequest;
import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Entity.User;
import com.billboarding.Services.Payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 1️⃣ Advertiser creates a Razorpay order for an APPROVED booking
     */
    @PostMapping("/create-order")
    public ResponseEntity<PaymentOrderResponse> createOrder(
            @RequestBody CreatePaymentOrderRequest req,
            Authentication auth
    ) {
        User advertiser = (User) auth.getPrincipal();
        PaymentOrderResponse res = paymentService.createOrder(req, advertiser);
        return ResponseEntity.ok(res);
    }

    /**
     * 2️⃣ After Razorpay success, frontend calls this API to verify payment
     */
    @PostMapping("/verify")
    public ResponseEntity<Booking> verifyPayment(
            @RequestBody VerifyPaymentRequest req,
            Authentication auth
    ) {
        User advertiser = (User) auth.getPrincipal();
        Booking booking = paymentService.verifyAndCapture(req, advertiser);
        return ResponseEntity.ok(booking);
    }
}
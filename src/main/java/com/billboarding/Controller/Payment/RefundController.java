package com.billboarding.Controller.Payment;

import com.billboarding.Entity.User;
import com.billboarding.Services.Payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class RefundController {

    private final PaymentService paymentService;

    @PostMapping("/refund/{bookingId}")
    public ResponseEntity<String> refundPayment(
            @PathVariable Long bookingId,
            Authentication auth
    ) {
        User advertiser = (User) auth.getPrincipal();
        paymentService.initiateRefund(bookingId, advertiser);
        return ResponseEntity.ok("Refund initiated");
    }
}

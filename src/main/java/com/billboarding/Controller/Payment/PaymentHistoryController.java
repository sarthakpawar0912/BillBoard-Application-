package com.billboarding.Controller.Payment;

import com.billboarding.Entity.Payment.PaymentHistory;
import com.billboarding.Entity.User;
import com.billboarding.Repository.Payment.PaymentHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentHistoryController {

    private final PaymentHistoryRepository paymentRepo;

    /**
     * 1️⃣ Advertiser: My payment history
     *    GET /api/payments/my-payments
     */
    @GetMapping("/payments/my-payments")
    public ResponseEntity<List<PaymentHistory>> getMyPayments(Authentication auth) {
        User advertiser = (User) auth.getPrincipal();
        return ResponseEntity.ok(paymentRepo.findByAdvertiser(advertiser));
        // SECURITY: in SecurityConfig this falls under authenticated user (no role restriction).
    }

    /**
     * 2️⃣ Owner: Payments received for my billboards
     *    GET /api/owner/payments
     */
    @GetMapping("/owner/payments")
    public ResponseEntity<List<PaymentHistory>> getOwnerPayments(Authentication auth) {
        User owner = (User) auth.getPrincipal();
        return ResponseEntity.ok(paymentRepo.findByOwner(owner));
        // SECURITY: /api/owner/** is restricted to OWNER in SecurityConfig.
    }

    /**
     * 3️⃣ Admin: All payments in system
     *    GET /api/admin/payments
     */
    @GetMapping("/admin/payments")
    public ResponseEntity<List<PaymentHistory>> getAllPayments() {
        return ResponseEntity.ok(paymentRepo.findAll());
        // SECURITY: /api/admin/** is restricted to ADMIN in SecurityConfig.
    }
}
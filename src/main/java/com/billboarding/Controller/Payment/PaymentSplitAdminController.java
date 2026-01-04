package com.billboarding.Controller.Payment;

import com.billboarding.Entity.Payment.PaymentSplit;
import com.billboarding.Repository.Payment.PaymentSplitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/payments/splits")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PaymentSplitAdminController {

    private final PaymentSplitRepository paymentSplitRepo;

    // 🔹 List all payment splits
    @GetMapping
    public List<PaymentSplit> getAllSplits() {
        return paymentSplitRepo.findAll();
    }

    // 🔹 Get split by ID
    @GetMapping("/{id}")
    public PaymentSplit getById(@PathVariable Long id) {
        return paymentSplitRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment split not found"));
    }
}

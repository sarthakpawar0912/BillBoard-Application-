package com.billboarding.Controller.Payment;

import com.billboarding.Entity.User;
import com.billboarding.Services.Payment.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/advertiser/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodController {

    private final PaymentMethodService service;

    @GetMapping
    public ResponseEntity<?> list(Authentication auth) {
        return ResponseEntity.ok(
                service.getMyMethods((User) auth.getPrincipal())
        );
    }

    @PostMapping
    public ResponseEntity<?> add(
            @RequestParam String type,
            @RequestParam String label,
            Authentication auth) {

        return ResponseEntity.ok(
                service.addMethod((User) auth.getPrincipal(), type, label)
        );
    }

    @PostMapping("/{id}/default")
    public ResponseEntity<?> setDefault(@PathVariable Long id, Authentication auth) {
        service.setDefault((User) auth.getPrincipal(), id);
        return ResponseEntity.ok("Default updated");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        service.remove((User) auth.getPrincipal(), id);
        return ResponseEntity.ok("Removed");
    }
}

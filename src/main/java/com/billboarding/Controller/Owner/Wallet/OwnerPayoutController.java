package com.billboarding.Controller.Owner.Wallet;

import com.billboarding.Entity.OWNER.wallet.PayoutRequest;
import com.billboarding.Entity.User;
import com.billboarding.Services.Owner.Wallet.OwnerPayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/owner/payouts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
public class OwnerPayoutController {

    private final OwnerPayoutService service;

    @PostMapping("/request")
    public ResponseEntity<PayoutRequest> requestPayout(
            @RequestParam Double amount,
            Authentication auth
    ) {
        User owner = (User) auth.getPrincipal();
        PayoutRequest payout = service.requestPayout(owner, amount);
        return ResponseEntity.ok(payout);
    }

    @GetMapping
    public ResponseEntity<List<PayoutRequest>> getMyPayouts(Authentication auth) {
        User owner = (User) auth.getPrincipal();
        return ResponseEntity.ok(service.getOwnerPayouts(owner));
    }
}

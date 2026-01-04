package com.billboarding.Controller.Admin.Wallet;

import com.billboarding.DTO.ADMIN.Wallet.AdminPayoutResponse;
import com.billboarding.Entity.OWNER.wallet.PayoutRequest;
import com.billboarding.Services.Admin.Wallet.AdminPayoutService;
import com.billboarding.Services.Owner.Wallet.OwnerPayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin controller for:
 * 1. Viewing OWNER's payout history (/api/admin/owner-payouts)
 * 2. Viewing ADMIN's own payout/withdrawal history (/api/admin/payouts)
 */
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPayoutController {

    private final OwnerPayoutService ownerPayoutService;
    private final AdminPayoutService adminPayoutService;

    // ==================== OWNER PAYOUT HISTORY (View Only) ====================

    /**
     * View all owner payout requests
     * Admin can see when owners withdraw money from their wallets
     */
    @GetMapping("/api/admin/owner-payouts")
    public ResponseEntity<List<PayoutRequest>> getAllOwnerPayouts() {
        return ResponseEntity.ok(ownerPayoutService.getAllPayouts());
    }

    /**
     * View owner payouts by status
     */
    @GetMapping("/api/admin/owner-payouts/status/{status}")
    public ResponseEntity<List<PayoutRequest>> getOwnerPayoutsByStatus(
            @PathVariable String status
    ) {
        return ResponseEntity.ok(ownerPayoutService.getPayoutsByStatus(status.toUpperCase()));
    }

    // ==================== ADMIN'S OWN PAYOUT HISTORY ====================

    /**
     * View all admin withdrawal/payout history
     * These are withdrawals from the admin commission wallet
     */
    @GetMapping("/api/admin/payouts")
    public ResponseEntity<List<AdminPayoutResponse>> getAllAdminPayouts() {
        return ResponseEntity.ok(adminPayoutService.getAllPayouts());
    }

    /**
     * View admin payouts by status
     */
    @GetMapping("/api/admin/payouts/status/{status}")
    public ResponseEntity<List<AdminPayoutResponse>> getAdminPayoutsByStatus(
            @PathVariable String status
    ) {
        return ResponseEntity.ok(adminPayoutService.getPayoutsByStatus(status.toUpperCase()));
    }

    /**
     * Get a specific admin payout by ID
     */
    @GetMapping("/api/admin/payouts/{id}")
    public ResponseEntity<AdminPayoutResponse> getAdminPayoutById(@PathVariable Long id) {
        return ResponseEntity.ok(adminPayoutService.getPayoutById(id));
    }
}

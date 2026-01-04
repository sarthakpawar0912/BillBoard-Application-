package com.billboarding.Controller.Admin.Wallet;

import com.billboarding.DTO.ADMIN.Wallet.AdminBankAccountRequest;
import com.billboarding.DTO.ADMIN.Wallet.AdminBankAccountResponse;
import com.billboarding.DTO.ADMIN.Wallet.AdminPayoutResponse;
import com.billboarding.Services.Admin.Wallet.AdminBankAccountService;
import com.billboarding.Services.Admin.Wallet.AdminPayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/bank-accounts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminBankAccountController {

    private final AdminBankAccountService bankAccountService;
    private final AdminPayoutService payoutService;

    // ==================== BANK ACCOUNT ENDPOINTS ====================

    @GetMapping
    public ResponseEntity<List<AdminBankAccountResponse>> getAllBankAccounts() {
        return ResponseEntity.ok(bankAccountService.getAllBankAccounts());
    }

    @GetMapping("/primary")
    public ResponseEntity<AdminBankAccountResponse> getPrimaryBankAccount() {
        AdminBankAccountResponse primary = bankAccountService.getPrimaryBankAccount();
        if (primary == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(primary);
    }

    @PostMapping
    public ResponseEntity<?> addBankAccount(@RequestBody AdminBankAccountRequest request) {
        try {
            AdminBankAccountResponse response = bankAccountService.addBankAccount(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBankAccount(
            @PathVariable Long id,
            @RequestBody AdminBankAccountRequest request) {
        try {
            AdminBankAccountResponse response = bankAccountService.updateBankAccount(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBankAccount(@PathVariable Long id) {
        try {
            bankAccountService.deleteBankAccount(id);
            return ResponseEntity.ok(Map.of("message", "Bank account deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/set-primary")
    public ResponseEntity<?> setPrimaryAccount(@PathVariable Long id) {
        try {
            AdminBankAccountResponse response = bankAccountService.setPrimaryAccount(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/verify")
    public ResponseEntity<?> verifyBankAccount(@PathVariable Long id) {
        try {
            AdminBankAccountResponse response = bankAccountService.verifyBankAccount(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ==================== PAYOUT ENDPOINTS ====================

    @GetMapping("/payouts")
    public ResponseEntity<List<AdminPayoutResponse>> getAllPayouts() {
        return ResponseEntity.ok(payoutService.getAllPayouts());
    }

    @GetMapping("/payouts/{id}")
    public ResponseEntity<?> getPayoutById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(payoutService.getPayoutById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/payouts/status/{status}")
    public ResponseEntity<List<AdminPayoutResponse>> getPayoutsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(payoutService.getPayoutsByStatus(status.toUpperCase()));
    }
}

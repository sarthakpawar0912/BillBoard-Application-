package com.billboarding.Controller.Owner.Bank;

import com.billboarding.DTO.OWNER.Bank.BankAccountDTO;
import com.billboarding.DTO.OWNER.Bank.BankAccountResponse;
import com.billboarding.Entity.User;
import com.billboarding.Services.Owner.Bank.OwnerBankAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/owner/bank-account")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
public class OwnerBankAccountController {

    private final OwnerBankAccountService bankAccountService;

    /**
     * Get owner's bank account details
     */
    @GetMapping
    public ResponseEntity<?> getBankAccount(Authentication auth) {
        User owner = (User) auth.getPrincipal();
        return bankAccountService.getBankAccount(owner)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    /**
     * Check if owner has bank account
     */
    @GetMapping("/exists")
    public ResponseEntity<Map<String, Boolean>> hasBankAccount(Authentication auth) {
        User owner = (User) auth.getPrincipal();
        boolean exists = bankAccountService.hasBankAccount(owner);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /**
     * Add or update bank account details
     */
    @PostMapping
    public ResponseEntity<?> saveBankAccount(
            Authentication auth,
            @Valid @RequestBody BankAccountDTO dto
    ) {
        try {
            User owner = (User) auth.getPrincipal();
            BankAccountResponse response = bankAccountService.saveBankAccount(owner, dto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete bank account
     */
    @DeleteMapping
    public ResponseEntity<?> deleteBankAccount(Authentication auth) {
        try {
            User owner = (User) auth.getPrincipal();
            bankAccountService.deleteBankAccount(owner);
            return ResponseEntity.ok(Map.of("message", "Bank account deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lookup bank name from IFSC code
     */
    @GetMapping("/lookup-ifsc/{ifsc}")
    public ResponseEntity<?> lookupIFSC(@PathVariable String ifsc) {
        String bankName = bankAccountService.getBankNameFromIFSC(ifsc);
        boolean valid = bankAccountService.isValidIFSC(ifsc.toUpperCase());

        return ResponseEntity.ok(Map.of(
                "ifsc", ifsc.toUpperCase(),
                "valid", valid,
                "bankName", bankName != null ? bankName : "Unknown Bank"
        ));
    }
}

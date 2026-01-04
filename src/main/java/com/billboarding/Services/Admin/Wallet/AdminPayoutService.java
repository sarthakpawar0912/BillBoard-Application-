package com.billboarding.Services.Admin.Wallet;

import com.billboarding.DTO.ADMIN.Wallet.AdminPayoutResponse;
import com.billboarding.Entity.ADMIN.wallet.AdminBankAccount;
import com.billboarding.Entity.ADMIN.wallet.AdminPayout;
import com.billboarding.Entity.ADMIN.wallet.AdminWallet;
import com.billboarding.Repository.Admin.Wallet.AdminPayoutRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminPayoutService {

    private final AdminPayoutRepository payoutRepo;
    private final AdminBankAccountService bankAccountService;
    private final AdminWalletService walletService;

    public List<AdminPayoutResponse> getAllPayouts() {
        return payoutRepo.findAllByOrderByInitiatedAtDesc()
                .stream()
                .map(AdminPayoutResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<AdminPayoutResponse> getPayoutsByStatus(String status) {
        return payoutRepo.findByStatusOrderByInitiatedAtDesc(status)
                .stream()
                .map(AdminPayoutResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public AdminPayoutResponse initiateWithdrawal(Double amount, String notes) {
        // Get wallet and validate balance
        AdminWallet wallet = walletService.getWallet();
        Double availableBalance = wallet.getBalance() - wallet.getPendingWithdrawal();

        if (amount <= 0) {
            throw new RuntimeException("Withdrawal amount must be greater than 0");
        }

        if (amount > availableBalance) {
            throw new RuntimeException("Insufficient balance. Available: " + availableBalance);
        }

        // Get primary bank account
        AdminBankAccount bankAccount = bankAccountService.getPrimaryBankAccountEntity();
        if (bankAccount == null) {
            throw new RuntimeException("No bank account configured. Please add a bank account first.");
        }

        // Create payout record
        AdminPayout payout = AdminPayout.builder()
                .amount(amount)
                .status("PENDING")
                .bankAccount(bankAccount)
                .notes(notes)
                .build();

        AdminPayout savedPayout = payoutRepo.save(payout);

        // Update pending withdrawal amount in wallet
        wallet.setPendingWithdrawal(wallet.getPendingWithdrawal() + amount);

        log.info("Withdrawal initiated: {} to bank account ending in {}",
                amount, bankAccount.getAccountNumber().substring(bankAccount.getAccountNumber().length() - 4));

        // For test mode: Auto-process the payout (simulate immediate transfer)
        processPayoutTestMode(savedPayout);

        // Refresh payout from DB
        AdminPayout processed = payoutRepo.findById(savedPayout.getId()).orElse(savedPayout);
        return AdminPayoutResponse.fromEntity(processed);
    }

    @Transactional
    public void processPayoutTestMode(AdminPayout payout) {
        try {
            // Simulate processing
            payout.setStatus("PROCESSING");
            payout.setProcessedAt(LocalDateTime.now());
            payoutRepo.save(payout);

            // In test mode, we simulate a successful bank transfer
            // Generate a fake UTR number for testing
            String fakeUtr = "UTR" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

            // Mark as completed
            payout.setStatus("COMPLETED");
            payout.setUtrNumber(fakeUtr);
            payout.setCompletedAt(LocalDateTime.now());
            payoutRepo.save(payout);

            // Debit the wallet
            walletService.debit(payout.getAmount(), "WITHDRAWAL#PAYOUT#" + payout.getId());

            // Clear pending withdrawal
            AdminWallet wallet = walletService.getWallet();
            wallet.setPendingWithdrawal(Math.max(0, wallet.getPendingWithdrawal() - payout.getAmount()));

            log.info("Payout {} completed successfully. UTR: {}", payout.getId(), fakeUtr);

        } catch (Exception e) {
            log.error("Payout processing failed for payout {}: {}", payout.getId(), e.getMessage());
            payout.setStatus("FAILED");
            payout.setFailureReason(e.getMessage());
            payoutRepo.save(payout);

            // Clear pending withdrawal on failure
            AdminWallet wallet = walletService.getWallet();
            wallet.setPendingWithdrawal(Math.max(0, wallet.getPendingWithdrawal() - payout.getAmount()));

            throw new RuntimeException("Payout failed: " + e.getMessage());
        }
    }

    // This method would be used in production with actual Razorpay X integration
    @Transactional
    public void processPayoutWithRazorpay(AdminPayout payout) {
        // In production, this would:
        // 1. Create a Razorpay X Payout using their API
        // 2. Wait for webhook callback or poll for status
        // 3. Update payout record based on Razorpay response

        // For now, delegate to test mode
        processPayoutTestMode(payout);
    }

    public AdminPayoutResponse getPayoutById(Long id) {
        return payoutRepo.findById(id)
                .map(AdminPayoutResponse::fromEntity)
                .orElseThrow(() -> new RuntimeException("Payout not found"));
    }
}

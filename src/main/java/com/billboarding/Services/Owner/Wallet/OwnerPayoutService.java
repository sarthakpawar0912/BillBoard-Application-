package com.billboarding.Services.Owner.Wallet;

import com.billboarding.Entity.OWNER.wallet.OwnerWallet;
import com.billboarding.Entity.OWNER.wallet.PayoutRequest;
import com.billboarding.Entity.User;
import com.billboarding.Repository.Owner.Wallet.OwnerWalletRepository;
import com.billboarding.Repository.Owner.Wallet.PayoutRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OwnerPayoutService {

    private final OwnerWalletRepository walletRepo;
    private final PayoutRequestRepository payoutRepo;
    private final OwnerWalletService walletService;

    // Test mode bank details (simulated)
    private static final String[] TEST_BANKS = {
            "HDFC Bank", "ICICI Bank", "State Bank of India", "Axis Bank", "Kotak Mahindra Bank"
    };

    /**
     * Owner requests a payout - Simulates immediate processing like Razorpay test mode
     */
    @Transactional
    public PayoutRequest requestPayout(User owner, Double amount) {
        OwnerWallet wallet = walletRepo.findByOwner(owner)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (wallet.getBalance() < amount) {
            throw new RuntimeException("Insufficient balance. Available: ₹" + String.format("%.2f", wallet.getBalance()));
        }

        if (amount <= 0) {
            throw new RuntimeException("Amount must be greater than 0");
        }

        if (amount < 100) {
            throw new RuntimeException("Minimum payout amount is ₹100");
        }

        // Generate simulated Razorpay payout ID and UTR
        String razorpayPayoutId = generateRazorpayPayoutId();
        String utrNumber = generateUTRNumber();
        String bankName = TEST_BANKS[new Random().nextInt(TEST_BANKS.length)];
        String maskedAccount = "XXXX" + String.format("%04d", new Random().nextInt(10000));

        // Debit wallet immediately (test mode - instant processing)
        walletService.debit(owner, amount, "PAYOUT#" + razorpayPayoutId);

        // Create payout request with PAID status (test mode simulates instant success)
        PayoutRequest payout = PayoutRequest.builder()
                .owner(owner)
                .amount(amount)
                .status("PAID")
                .razorpayPayoutId(razorpayPayoutId)
                .utrNumber(utrNumber)
                .bankName(bankName)
                .accountNumber(maskedAccount)
                .transferMode("IMPS")
                .processedAt(LocalDateTime.now())
                .build();

        return payoutRepo.save(payout);
    }

    /**
     * Generate simulated Razorpay Payout ID
     */
    private String generateRazorpayPayoutId() {
        return "pout_" + generateRandomAlphanumeric(14);
    }

    /**
     * Generate simulated UTR Number (like real bank transfers)
     */
    private String generateUTRNumber() {
        // Format: XXXXR520251231XXXXXX (Bank code + date + sequence)
        String bankCode = String.format("%04d", new Random().nextInt(10000));
        String date = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequence = String.format("%06d", new Random().nextInt(1000000));
        return bankCode + "R5" + date + sequence;
    }

    private String generateRandomAlphanumeric(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * Get all payout requests for an owner
     */
    public List<PayoutRequest> getOwnerPayouts(User owner) {
        return payoutRepo.findByOwnerOrderByCreatedAtDesc(owner);
    }

    /**
     * Get all payout requests (admin)
     */
    public List<PayoutRequest> getAllPayouts() {
        return payoutRepo.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Get payout requests by status (admin)
     */
    public List<PayoutRequest> getPayoutsByStatus(String status) {
        return payoutRepo.findByStatus(status);
    }
}

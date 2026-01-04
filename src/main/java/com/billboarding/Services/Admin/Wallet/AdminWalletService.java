package com.billboarding.Services.Admin.Wallet;

import com.billboarding.ENUM.TxType;
import com.billboarding.Entity.ADMIN.wallet.AdminWallet;
import com.billboarding.Entity.ADMIN.wallet.AdminWalletTransaction;
import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Repository.Admin.Wallet.AdminWalletRepository;
import com.billboarding.Repository.Admin.Wallet.AdminWalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminWalletService {

    private final AdminWalletRepository walletRepo;
    private final AdminWalletTransactionRepository txRepo;

    private static final Long ADMIN_WALLET_ID = 1L;

    // Get or create admin wallet (singleton)
    public AdminWallet getWallet() {
        return walletRepo.findById(ADMIN_WALLET_ID)
                .orElseGet(() -> walletRepo.save(
                        AdminWallet.builder()
                                .id(ADMIN_WALLET_ID)
                                .balance(0.0)
                                .totalEarned(0.0)
                                .totalWithdrawn(0.0)
                                .pendingWithdrawal(0.0)
                                .build()
                ));
    }

    // Credit commission from booking
    @Transactional
    public void credit(Double amount, String reference) {
        AdminWallet wallet = getWallet();
        wallet.setBalance(wallet.getBalance() + amount);
        wallet.setTotalEarned(wallet.getTotalEarned() + amount);
        walletRepo.save(wallet);

        txRepo.save(
                AdminWalletTransaction.builder()
                        .amount(amount)
                        .type(TxType.CREDIT)
                        .reference(reference)
                        .description("Commission from booking")
                        .balanceAfter(wallet.getBalance())
                        .time(LocalDateTime.now())
                        .build()
        );
    }

    // Enhanced credit with booking details
    @Transactional
    public void creditFromBooking(Booking booking, Double commissionAmount) {
        AdminWallet wallet = getWallet();
        wallet.setBalance(wallet.getBalance() + commissionAmount);
        wallet.setTotalEarned(wallet.getTotalEarned() + commissionAmount);
        walletRepo.save(wallet);

        txRepo.save(
                AdminWalletTransaction.builder()
                        .amount(commissionAmount)
                        .type(TxType.CREDIT)
                        .reference("COMMISSION#BOOKING#" + booking.getId())
                        .description("Commission from booking #" + booking.getId() +
                                " - Billboard: " + booking.getBillboard().getTitle())
                        .bookingId(booking.getId())
                        .ownerId(booking.getBillboard().getOwner().getId())
                        .balanceAfter(wallet.getBalance())
                        .time(LocalDateTime.now())
                        .build()
        );
    }

    // Debit from wallet (for payouts/withdrawals)
    @Transactional
    public void debit(Double amount, String reference) {
        AdminWallet wallet = getWallet();

        if (wallet.getBalance() < amount) {
            throw new RuntimeException("Insufficient admin wallet balance");
        }

        wallet.setBalance(wallet.getBalance() - amount);
        wallet.setTotalWithdrawn(wallet.getTotalWithdrawn() + amount);
        walletRepo.save(wallet);

        txRepo.save(
                AdminWalletTransaction.builder()
                        .amount(amount)
                        .type(TxType.DEBIT)
                        .reference(reference)
                        .description("Admin withdrawal")
                        .balanceAfter(wallet.getBalance())
                        .time(LocalDateTime.now())
                        .build()
        );
    }

    // Debit for refund (when a booking is refunded, deduct commission)
    @Transactional
    public void debitForRefund(Booking booking, Double commissionAmount) {
        AdminWallet wallet = getWallet();

        if (wallet.getBalance() < commissionAmount) {
            throw new RuntimeException("Insufficient admin wallet balance for refund");
        }

        wallet.setBalance(wallet.getBalance() - commissionAmount);
        // Reduce total earned since this was a refund
        wallet.setTotalEarned(wallet.getTotalEarned() - commissionAmount);
        walletRepo.save(wallet);

        txRepo.save(
                AdminWalletTransaction.builder()
                        .amount(commissionAmount)
                        .type(TxType.DEBIT)
                        .reference("REFUND#BOOKING#" + booking.getId())
                        .description("Commission refund for booking #" + booking.getId())
                        .bookingId(booking.getId())
                        .ownerId(booking.getBillboard().getOwner().getId())
                        .balanceAfter(wallet.getBalance())
                        .time(LocalDateTime.now())
                        .build()
        );
    }

    // Get all transactions (ordered by id DESC for reliable sequential ordering)
    public List<AdminWalletTransaction> getTransactions() {
        return txRepo.findAllByOrderByIdDesc();
    }

    // Get transactions with pagination
    public List<AdminWalletTransaction> getTransactions(int limit, int offset) {
        List<AdminWalletTransaction> all = txRepo.findAllByOrderByIdDesc();
        int end = Math.min(offset + limit, all.size());
        if (offset >= all.size()) {
            return List.of();
        }
        return all.subList(offset, end);
    }

    // Get transactions by type
    public List<AdminWalletTransaction> getTransactionsByType(TxType type) {
        return txRepo.findByTypeOrderByIdDesc(type);
    }

    // Get transactions by date range
    public List<AdminWalletTransaction> getTransactionsByDateRange(LocalDateTime start, LocalDateTime end) {
        return txRepo.findByTimeBetweenOrderByIdDesc(start, end);
    }

    // Get commission analytics
    public Map<String, Object> getCommissionAnalytics() {
        AdminWallet wallet = getWallet();
        List<AdminWalletTransaction> credits = txRepo.findByTypeOrderByIdDesc(TxType.CREDIT);
        List<AdminWalletTransaction> debits = txRepo.findByTypeOrderByIdDesc(TxType.DEBIT);

        // Calculate today's earnings
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        double todayEarnings = credits.stream()
                .filter(tx -> tx.getTime().isAfter(startOfDay))
                .mapToDouble(AdminWalletTransaction::getAmount)
                .sum();

        // Calculate this month's earnings
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        double monthEarnings = credits.stream()
                .filter(tx -> tx.getTime().isAfter(startOfMonth))
                .mapToDouble(AdminWalletTransaction::getAmount)
                .sum();

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("currentBalance", wallet.getBalance());
        analytics.put("totalEarned", wallet.getTotalEarned());
        analytics.put("totalWithdrawn", wallet.getTotalWithdrawn());
        analytics.put("pendingWithdrawal", wallet.getPendingWithdrawal());
        analytics.put("todayEarnings", todayEarnings);
        analytics.put("monthEarnings", monthEarnings);
        analytics.put("totalTransactions", credits.size() + debits.size());
        analytics.put("totalCredits", credits.size());
        analytics.put("totalDebits", debits.size());

        return analytics;
    }
}

package com.billboarding.Controller.Admin.Wallet;

import com.billboarding.DTO.ADMIN.Wallet.*;
import com.billboarding.ENUM.TxType;
import com.billboarding.Entity.ADMIN.wallet.AdminWallet;
import com.billboarding.Entity.ADMIN.wallet.AdminWalletTransaction;
import com.billboarding.Repository.UserRepository;
import com.billboarding.Services.Admin.Wallet.AdminPayoutService;
import com.billboarding.Services.Admin.Wallet.AdminWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/wallet")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminWalletController {

    private final AdminWalletService service;
    private final AdminPayoutService payoutService;
    private final UserRepository userRepository;

    // ================= WALLET BALANCE =================

    @GetMapping
    public ResponseEntity<AdminWalletResponse> getWallet() {
        AdminWallet wallet = service.getWallet();
        AdminWalletResponse response = AdminWalletResponse.builder()
                .currentBalance(wallet.getBalance())
                .totalEarned(wallet.getTotalEarned())
                .totalWithdrawn(wallet.getTotalWithdrawn())
                .pendingWithdrawal(wallet.getPendingWithdrawal())
                .availableForWithdrawal(wallet.getBalance() - wallet.getPendingWithdrawal())
                .updatedAt(wallet.getUpdatedAt())
                .build();
        return ResponseEntity.ok(response);
    }

    // ================= TRANSACTIONS =================

    @GetMapping("/transactions")
    public ResponseEntity<List<AdminTransactionResponse>> getTransactions(
            @RequestParam(required = false) TxType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        List<AdminWalletTransaction> transactions;

        if (type != null) {
            transactions = service.getTransactionsByType(type);
        } else if (startDate != null && endDate != null) {
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.atTime(23, 59, 59);
            transactions = service.getTransactionsByDateRange(start, end);
        } else {
            transactions = service.getTransactions(limit, offset);
        }

        List<AdminTransactionResponse> response = transactions.stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<AdminTransactionResponse> getTransaction(@PathVariable Long id) {
        AdminWalletTransaction tx = service.getTransactions().stream()
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        return ResponseEntity.ok(mapToTransactionResponse(tx));
    }

    // ================= COMMISSION ANALYTICS =================

    @GetMapping("/analytics")
    public ResponseEntity<CommissionAnalyticsResponse> getCommissionAnalytics() {
        AdminWallet wallet = service.getWallet();
        List<AdminWalletTransaction> allTx = service.getTransactions();
        List<AdminWalletTransaction> credits = allTx.stream()
                .filter(tx -> tx.getType() == TxType.CREDIT)
                .collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime startOfWeek = LocalDate.now().minusDays(7).atStartOfDay();
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfYear = LocalDate.now().withDayOfYear(1).atStartOfDay();

        double todayEarnings = credits.stream()
                .filter(tx -> tx.getTime().isAfter(startOfDay))
                .mapToDouble(AdminWalletTransaction::getAmount)
                .sum();

        double weekEarnings = credits.stream()
                .filter(tx -> tx.getTime().isAfter(startOfWeek))
                .mapToDouble(AdminWalletTransaction::getAmount)
                .sum();

        double monthEarnings = credits.stream()
                .filter(tx -> tx.getTime().isAfter(startOfMonth))
                .mapToDouble(AdminWalletTransaction::getAmount)
                .sum();

        double yearEarnings = credits.stream()
                .filter(tx -> tx.getTime().isAfter(startOfYear))
                .mapToDouble(AdminWalletTransaction::getAmount)
                .sum();

        // Statistics
        DoubleSummaryStatistics stats = credits.stream()
                .mapToDouble(AdminWalletTransaction::getAmount)
                .summaryStatistics();

        // Monthly breakdown (last 12 months)
        List<MonthlyCommissionDTO> monthlyBreakdown = getMonthlyBreakdown(credits);

        CommissionAnalyticsResponse response = CommissionAnalyticsResponse.builder()
                .currentBalance(wallet.getBalance())
                .totalEarned(wallet.getTotalEarned())
                .totalWithdrawn(wallet.getTotalWithdrawn())
                .pendingWithdrawal(wallet.getPendingWithdrawal())
                .todayEarnings(todayEarnings)
                .weekEarnings(weekEarnings)
                .monthEarnings(monthEarnings)
                .yearEarnings(yearEarnings)
                .totalTransactions((long) allTx.size())
                .totalCredits((long) credits.size())
                .totalDebits((long) (allTx.size() - credits.size()))
                .averageCommissionPerBooking(credits.isEmpty() ? 0.0 : stats.getAverage())
                .highestCommission(credits.isEmpty() ? 0.0 : stats.getMax())
                .lowestCommission(credits.isEmpty() ? 0.0 : stats.getMin())
                .monthlyBreakdown(monthlyBreakdown)
                .build();

        return ResponseEntity.ok(response);
    }

    // ================= WITHDRAWAL (Admin can withdraw commission to bank) =================

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdrawCommission(
            @RequestBody AdminWithdrawalRequest request
    ) {
        try {
            // Use the payout service which handles bank transfer simulation
            AdminPayoutResponse payout = payoutService.initiateWithdrawal(
                    request.getAmount(),
                    request.getNotes()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Withdrawal of ₹" + request.getAmount() + " processed successfully");
            response.put("amountWithdrawn", request.getAmount());
            response.put("newBalance", service.getWallet().getBalance());
            response.put("payout", payout);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // ================= DASHBOARD SUMMARY =================

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        AdminWallet wallet = service.getWallet();
        List<AdminWalletTransaction> recentTx = service.getTransactions(10, 0);

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("wallet", AdminWalletResponse.builder()
                .currentBalance(wallet.getBalance())
                .totalEarned(wallet.getTotalEarned())
                .totalWithdrawn(wallet.getTotalWithdrawn())
                .pendingWithdrawal(wallet.getPendingWithdrawal())
                .availableForWithdrawal(wallet.getBalance() - wallet.getPendingWithdrawal())
                .updatedAt(wallet.getUpdatedAt())
                .build());

        dashboard.put("recentTransactions", recentTx.stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList()));

        // Quick stats
        Map<String, Object> quickStats = service.getCommissionAnalytics();
        dashboard.put("quickStats", quickStats);

        return ResponseEntity.ok(dashboard);
    }

    // ================= HELPER METHODS =================

    private AdminTransactionResponse mapToTransactionResponse(AdminWalletTransaction tx) {
        String ownerName = null;
        if (tx.getOwnerId() != null) {
            ownerName = userRepository.findById(tx.getOwnerId())
                    .map(u -> u.getName())
                    .orElse("Unknown");
        }

        return AdminTransactionResponse.builder()
                .id(tx.getId())
                .amount(tx.getAmount())
                .type(tx.getType())
                .reference(tx.getReference())
                .description(tx.getDescription())
                .bookingId(tx.getBookingId())
                .ownerId(tx.getOwnerId())
                .ownerName(ownerName)
                .balanceAfter(tx.getBalanceAfter())
                .time(tx.getTime())
                .build();
    }

    private List<MonthlyCommissionDTO> getMonthlyBreakdown(List<AdminWalletTransaction> credits) {
        Map<String, List<AdminWalletTransaction>> byMonth = credits.stream()
                .collect(Collectors.groupingBy(tx -> {
                    LocalDateTime time = tx.getTime();
                    return time.getYear() + "-" + String.format("%02d", time.getMonthValue());
                }));

        List<MonthlyCommissionDTO> breakdown = new ArrayList<>();

        // Get last 12 months
        LocalDate now = LocalDate.now();
        for (int i = 0; i < 12; i++) {
            LocalDate month = now.minusMonths(i);
            String key = month.getYear() + "-" + String.format("%02d", month.getMonthValue());
            String monthName = month.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + month.getYear();

            List<AdminWalletTransaction> monthTx = byMonth.getOrDefault(key, List.of());
            double amount = monthTx.stream().mapToDouble(AdminWalletTransaction::getAmount).sum();

            breakdown.add(MonthlyCommissionDTO.builder()
                    .month(key)
                    .monthName(monthName)
                    .amount(amount)
                    .transactionCount((long) monthTx.size())
                    .build());
        }

        return breakdown;
    }
}

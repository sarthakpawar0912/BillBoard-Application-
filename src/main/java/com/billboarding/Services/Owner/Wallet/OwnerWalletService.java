package com.billboarding.Services.Owner.Wallet;

import com.billboarding.ENUM.TxType;
import com.billboarding.Entity.OWNER.wallet.OwnerWallet;
import com.billboarding.Entity.OWNER.wallet.WalletTransaction;
import com.billboarding.Entity.User;
import com.billboarding.Repository.Owner.Wallet.OwnerWalletRepository;
import com.billboarding.Repository.Owner.Wallet.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OwnerWalletService {

    private final OwnerWalletRepository walletRepo;
    private final WalletTransactionRepository txRepo;

    // CREDIT from booking
    @Transactional
    public void credit(User owner, Double amount, String ref) {

        OwnerWallet wallet = walletRepo
                .findByOwner(owner)
                .orElse(
                        OwnerWallet.builder()
                                .owner(owner)
                                .balance(0.0)
                                .totalEarned(0.0)
                                .totalWithdrawn(0.0)
                                .build()
                );

        wallet.setBalance(wallet.getBalance() + amount);
        if (wallet.getTotalEarned() == null) {
            wallet.setTotalEarned(0.0);
        }
        wallet.setTotalEarned(wallet.getTotalEarned() + amount);
        walletRepo.save(wallet);

        txRepo.save(
                WalletTransaction.builder()
                        .owner(owner)
                        .amount(amount)
                        .type(TxType.CREDIT)
                        .reference(ref)
                        .time(LocalDateTime.now())
                        .build()
        );
    }

    // DEBIT after payout
    @Transactional
    public void debit(User owner, Double amount, String ref) {

        OwnerWallet wallet = walletRepo
                .findByOwner(owner)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (wallet.getBalance() < amount) {
            throw new RuntimeException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance() - amount);
        if (wallet.getTotalWithdrawn() == null) {
            wallet.setTotalWithdrawn(0.0);
        }
        wallet.setTotalWithdrawn(wallet.getTotalWithdrawn() + amount);
        walletRepo.save(wallet);

        txRepo.save(
                WalletTransaction.builder()
                        .owner(owner)
                        .amount(amount)
                        .type(TxType.DEBIT)
                        .reference(ref)
                        .time(LocalDateTime.now())
                        .build()
        );
    }

    // AUTO-CREATE WALLET IF NOT EXISTS
    public OwnerWallet getWallet(User owner) {
        return walletRepo.findByOwner(owner)
                .orElseGet(() ->
                        walletRepo.save(
                                OwnerWallet.builder()
                                        .owner(owner)
                                        .balance(0.0)
                                        .totalEarned(0.0)
                                        .totalWithdrawn(0.0)
                                        .build()
                        )
                );
    }

    // Get all transactions for owner
    public List<WalletTransaction> getTransactions(User owner) {
        return txRepo.findByOwnerOrderByTimeDesc(owner);
    }
}

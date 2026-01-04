package com.billboarding.Repository.Owner.Wallet;

import com.billboarding.Entity.OWNER.wallet.WalletTransaction;
import com.billboarding.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletTransactionRepository
        extends JpaRepository<WalletTransaction, Long> {

    List<WalletTransaction> findByOwnerOrderByTimeDesc(User owner);
}

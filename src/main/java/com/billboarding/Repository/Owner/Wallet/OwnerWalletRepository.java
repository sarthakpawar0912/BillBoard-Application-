package com.billboarding.Repository.Owner.Wallet;

import com.billboarding.Entity.OWNER.wallet.OwnerWallet;
import com.billboarding.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OwnerWalletRepository extends JpaRepository<OwnerWallet, Long> {
    Optional<OwnerWallet> findByOwner(User owner);
}

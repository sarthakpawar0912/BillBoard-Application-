package com.billboarding.Repository.Admin.Wallet;

import com.billboarding.Entity.ADMIN.wallet.AdminWallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminWalletRepository extends JpaRepository<AdminWallet, Long> {
}

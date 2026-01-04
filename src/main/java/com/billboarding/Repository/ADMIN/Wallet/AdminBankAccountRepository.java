package com.billboarding.Repository.Admin.Wallet;

import com.billboarding.Entity.ADMIN.wallet.AdminBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface AdminBankAccountRepository extends JpaRepository<AdminBankAccount, Long> {

    Optional<AdminBankAccount> findByIsPrimaryTrue();

    List<AdminBankAccount> findAllByOrderByCreatedAtDesc();

    Optional<AdminBankAccount> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumberAndIfscCode(String accountNumber, String ifscCode);
}

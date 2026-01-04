package com.billboarding.Repository.Owner.Bank;

import com.billboarding.Entity.OWNER.bank.OwnerBankAccount;
import com.billboarding.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OwnerBankAccountRepository extends JpaRepository<OwnerBankAccount, Long> {

    Optional<OwnerBankAccount> findByOwner(User owner);

    Optional<OwnerBankAccount> findByOwnerId(Long ownerId);

    boolean existsByOwner(User owner);

    boolean existsByAccountNumberAndIfscCode(String accountNumber, String ifscCode);

    Optional<OwnerBankAccount> findByRazorpayFundAccountId(String fundAccountId);
}

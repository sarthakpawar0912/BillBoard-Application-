package com.billboarding.Services.Admin.Wallet;

import com.billboarding.DTO.ADMIN.Wallet.AdminBankAccountRequest;
import com.billboarding.DTO.ADMIN.Wallet.AdminBankAccountResponse;
import com.billboarding.Entity.ADMIN.wallet.AdminBankAccount;
import com.billboarding.Repository.Admin.Wallet.AdminBankAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminBankAccountService {

    private final AdminBankAccountRepository bankAccountRepo;

    public List<AdminBankAccountResponse> getAllBankAccounts() {
        return bankAccountRepo.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(AdminBankAccountResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public AdminBankAccountResponse getPrimaryBankAccount() {
        return bankAccountRepo.findByIsPrimaryTrue()
                .map(AdminBankAccountResponse::fromEntity)
                .orElse(null);
    }

    public AdminBankAccount getPrimaryBankAccountEntity() {
        return bankAccountRepo.findByIsPrimaryTrue().orElse(null);
    }

    @Transactional
    public AdminBankAccountResponse addBankAccount(AdminBankAccountRequest request) {
        // Validate account numbers match
        if (!request.getAccountNumber().equals(request.getConfirmAccountNumber())) {
            throw new RuntimeException("Account numbers do not match");
        }

        // Check if account already exists
        if (bankAccountRepo.existsByAccountNumberAndIfscCode(
                request.getAccountNumber(), request.getIfscCode())) {
            throw new RuntimeException("This bank account is already added");
        }

        // If this is the first account, make it primary
        boolean hasPrimary = bankAccountRepo.findByIsPrimaryTrue().isPresent();

        AdminBankAccount account = AdminBankAccount.builder()
                .accountHolderName(request.getAccountHolderName())
                .accountNumber(request.getAccountNumber())
                .ifscCode(request.getIfscCode().toUpperCase())
                .bankName(request.getBankName())
                .branchName(request.getBranchName())
                .accountType(request.getAccountType())
                .isPrimary(!hasPrimary)
                .isVerified(false)
                .build();

        AdminBankAccount saved = bankAccountRepo.save(account);
        return AdminBankAccountResponse.fromEntity(saved);
    }

    @Transactional
    public AdminBankAccountResponse updateBankAccount(Long id, AdminBankAccountRequest request) {
        AdminBankAccount account = bankAccountRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Bank account not found"));

        account.setAccountHolderName(request.getAccountHolderName());
        account.setBankName(request.getBankName());
        account.setBranchName(request.getBranchName());
        account.setAccountType(request.getAccountType());

        // Only update account number and IFSC if they've changed
        if (!account.getAccountNumber().equals(request.getAccountNumber())) {
            if (!request.getAccountNumber().equals(request.getConfirmAccountNumber())) {
                throw new RuntimeException("Account numbers do not match");
            }
            account.setAccountNumber(request.getAccountNumber());
            account.setIsVerified(false); // Re-verification needed
        }

        if (!account.getIfscCode().equals(request.getIfscCode())) {
            account.setIfscCode(request.getIfscCode().toUpperCase());
            account.setIsVerified(false);
        }

        AdminBankAccount saved = bankAccountRepo.save(account);
        return AdminBankAccountResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteBankAccount(Long id) {
        AdminBankAccount account = bankAccountRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Bank account not found"));

        if (account.getIsPrimary()) {
            throw new RuntimeException("Cannot delete primary bank account. Set another account as primary first.");
        }

        bankAccountRepo.delete(account);
    }

    @Transactional
    public AdminBankAccountResponse setPrimaryAccount(Long id) {
        // Remove primary from current primary account
        bankAccountRepo.findByIsPrimaryTrue().ifPresent(current -> {
            current.setIsPrimary(false);
            bankAccountRepo.save(current);
        });

        // Set new primary
        AdminBankAccount account = bankAccountRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Bank account not found"));

        account.setIsPrimary(true);
        AdminBankAccount saved = bankAccountRepo.save(account);
        return AdminBankAccountResponse.fromEntity(saved);
    }

    @Transactional
    public AdminBankAccountResponse verifyBankAccount(Long id) {
        AdminBankAccount account = bankAccountRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Bank account not found"));

        // In a real scenario, you would verify via penny testing or bank verification API
        // For test purposes, we'll just mark it as verified
        account.setIsVerified(true);
        AdminBankAccount saved = bankAccountRepo.save(account);
        return AdminBankAccountResponse.fromEntity(saved);
    }
}

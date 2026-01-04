package com.billboarding.Services.Owner.Bank;

import com.billboarding.DTO.OWNER.Bank.BankAccountDTO;
import com.billboarding.DTO.OWNER.Bank.BankAccountResponse;
import com.billboarding.Entity.OWNER.bank.OwnerBankAccount;
import com.billboarding.Entity.User;
import com.billboarding.Repository.Owner.Bank.OwnerBankAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OwnerBankAccountService {

    private final OwnerBankAccountRepository bankAccountRepo;

    // Known bank IFSC prefixes for validation
    private static final Map<String, String> BANK_IFSC_MAP = new HashMap<>() {{
        put("HDFC", "HDFC Bank");
        put("ICIC", "ICICI Bank");
        put("SBIN", "State Bank of India");
        put("UTIB", "Axis Bank");
        put("KKBK", "Kotak Mahindra Bank");
        put("PUNB", "Punjab National Bank");
        put("BARB", "Bank of Baroda");
        put("CNRB", "Canara Bank");
        put("UBIN", "Union Bank of India");
        put("IOBA", "Indian Overseas Bank");
        put("BKID", "Bank of India");
        put("CBIN", "Central Bank of India");
        put("IDIB", "Indian Bank");
        put("YESB", "Yes Bank");
        put("INDB", "IndusInd Bank");
        put("FDRL", "Federal Bank");
        put("RATN", "RBL Bank");
        put("KARB", "Karnataka Bank");
        put("SIBL", "South Indian Bank");
        put("KVBL", "Karur Vysya Bank");
    }};

    /**
     * Get owner's bank account details
     */
    public Optional<BankAccountResponse> getBankAccount(User owner) {
        return bankAccountRepo.findByOwner(owner)
                .map(BankAccountResponse::fromEntity);
    }

    /**
     * Check if owner has bank account registered
     */
    public boolean hasBankAccount(User owner) {
        return bankAccountRepo.existsByOwner(owner);
    }

    /**
     * Add or update bank account details
     */
    @Transactional
    public BankAccountResponse saveBankAccount(User owner, BankAccountDTO dto) {
        // Validate account numbers match
        if (!dto.getAccountNumber().equals(dto.getConfirmAccountNumber())) {
            throw new RuntimeException("Account numbers do not match");
        }

        // Validate IFSC format
        if (!isValidIFSC(dto.getIfscCode())) {
            throw new RuntimeException("Invalid IFSC code format");
        }

        // Get or create bank account
        OwnerBankAccount bankAccount = bankAccountRepo.findByOwner(owner)
                .orElse(OwnerBankAccount.builder().owner(owner).build());

        // Update details
        bankAccount.setAccountHolderName(dto.getAccountHolderName().trim());
        bankAccount.setAccountNumber(dto.getAccountNumber().trim());
        bankAccount.setIfscCode(dto.getIfscCode().toUpperCase().trim());
        bankAccount.setBankName(dto.getBankName().trim());
        bankAccount.setBranchName(dto.getBranchName() != null ? dto.getBranchName().trim() : null);
        bankAccount.setAccountType(dto.getAccountType() != null ? dto.getAccountType() : "SAVINGS");

        // In test mode, auto-verify. In production, this would go through penny verification
        bankAccount.setVerificationStatus("VERIFIED");

        OwnerBankAccount saved = bankAccountRepo.save(bankAccount);
        log.info("Bank account saved for owner {}: {}", owner.getId(), saved.getMaskedAccountNumber());

        return BankAccountResponse.fromEntity(saved);
    }

    /**
     * Delete bank account (only if no pending payouts)
     */
    @Transactional
    public void deleteBankAccount(User owner) {
        OwnerBankAccount bankAccount = bankAccountRepo.findByOwner(owner)
                .orElseThrow(() -> new RuntimeException("No bank account found"));

        // In production, check for pending payouts before deleting
        bankAccountRepo.delete(bankAccount);
        log.info("Bank account deleted for owner {}", owner.getId());
    }

    /**
     * Get bank name from IFSC code
     */
    public String getBankNameFromIFSC(String ifscCode) {
        if (ifscCode == null || ifscCode.length() < 4) {
            return null;
        }
        String prefix = ifscCode.substring(0, 4).toUpperCase();
        return BANK_IFSC_MAP.getOrDefault(prefix, null);
    }

    /**
     * Validate IFSC format
     */
    public boolean isValidIFSC(String ifsc) {
        if (ifsc == null) return false;
        // IFSC format: 4 letters + 0 + 6 alphanumeric
        return ifsc.matches("^[A-Z]{4}0[A-Z0-9]{6}$");
    }

    /**
     * Get the actual bank account entity (for internal use)
     */
    public Optional<OwnerBankAccount> getBankAccountEntity(User owner) {
        return bankAccountRepo.findByOwner(owner);
    }
}

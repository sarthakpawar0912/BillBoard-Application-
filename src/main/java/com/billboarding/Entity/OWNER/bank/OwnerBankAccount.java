package com.billboarding.Entity.OWNER.bank;

import com.billboarding.Entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Stores owner's bank account details for receiving payouts.
 * In production with RazorpayX, this will also store the Fund Account ID.
 */
@Entity
@Table(name = "owner_bank_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerBankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "owner_id", unique = true)
    @JsonIgnore
    private User owner;

    // Bank Account Details
    @Column(nullable = false)
    private String accountHolderName;

    @Column(nullable = false)
    private String accountNumber;

    @Column(nullable = false, length = 11)
    private String ifscCode;

    @Column(nullable = false)
    private String bankName;

    private String branchName;

    // Account type: SAVINGS, CURRENT
    @Column(nullable = false)
    @Builder.Default
    private String accountType = "SAVINGS";

    // Verification status: PENDING, VERIFIED, FAILED
    @Column(nullable = false)
    @Builder.Default
    private String verificationStatus = "PENDING";

    // RazorpayX Fund Account ID (for real payouts)
    // This is created when bank account is registered with RazorpayX
    private String razorpayFundAccountId;

    // RazorpayX Contact ID (owner as a contact in RazorpayX)
    private String razorpayContactId;

    // Primary account flag (for compatibility with existing database schema)
    @Column(name = "primary_account")
    @Builder.Default
    private boolean primaryAccount = true;

    // For display - masked account number (XXXX1234)
    @Transient
    public String getMaskedAccountNumber() {
        if (accountNumber == null || accountNumber.length() < 4) {
            return "XXXX";
        }
        return "XXXX" + accountNumber.substring(accountNumber.length() - 4);
    }

    // Is this account ready for payouts?
    @Transient
    public boolean isReadyForPayout() {
        return "VERIFIED".equals(verificationStatus) &&
               accountNumber != null &&
               ifscCode != null;
    }

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

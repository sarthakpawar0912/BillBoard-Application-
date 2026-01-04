package com.billboarding.Entity.ADMIN.wallet;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_bank_account")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AdminBankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String accountHolderName;

    @Column(nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private String ifscCode;

    private String bankName;

    private String branchName;

    @Column(length = 20)
    private String accountType; // SAVINGS, CURRENT

    @Builder.Default
    private Boolean isPrimary = true;

    @Builder.Default
    private Boolean isVerified = false;

    private String razorpayFundAccountId; // For Razorpay integration

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

package com.billboarding.Entity.ADMIN.wallet;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_payouts")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AdminPayout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id")
    private AdminBankAccount bankAccount;

    private String razorpayPayoutId;

    private String utrNumber; // Bank UTR for tracking

    private String notes;

    private String failureReason;

    private LocalDateTime initiatedAt;

    private LocalDateTime processedAt;

    private LocalDateTime completedAt;

    @PrePersist
    public void prePersist() {
        this.initiatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "PENDING";
        }
    }
}

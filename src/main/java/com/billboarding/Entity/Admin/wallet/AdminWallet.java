package com.billboarding.Entity.ADMIN.wallet;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_wallet")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AdminWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    private Double balance = 0.0;

    @Builder.Default
    private Double totalEarned = 0.0;

    @Builder.Default
    private Double totalWithdrawn = 0.0;

    @Builder.Default
    private Double pendingWithdrawal = 0.0;

    private LocalDateTime updatedAt;

    @PreUpdate
    @PrePersist
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}

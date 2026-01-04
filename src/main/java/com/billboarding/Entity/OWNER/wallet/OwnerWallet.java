package com.billboarding.Entity.OWNER.wallet;

import com.billboarding.Entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "owner_wallets",
       uniqueConstraints = @UniqueConstraint(columnNames = "owner_id"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class OwnerWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    private User owner;

    private Double balance;

    private Double totalEarned;

    private Double totalWithdrawn;

    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void updateTime() {
        updatedAt = LocalDateTime.now();
    }
}

package com.billboarding.Entity.ADMIN.wallet;

import com.billboarding.ENUM.TxType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_wallet_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminWalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private TxType type; // CREDIT / DEBIT

    private String reference;

    private String description;

    private Long bookingId;

    private Long ownerId;

    private Double balanceAfter;

    private LocalDateTime time;
}

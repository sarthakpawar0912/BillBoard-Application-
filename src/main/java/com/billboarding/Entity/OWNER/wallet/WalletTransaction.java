package com.billboarding.Entity.OWNER.wallet;

import com.billboarding.ENUM.TxType;
import com.billboarding.Entity.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User owner;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private TxType type; // CREDIT / DEBIT ✅ CORRECT

    private String reference;

    private LocalDateTime time;
}

package com.billboarding.Entity.OWNER.wallet;

import com.billboarding.Entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payout_requests")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PayoutRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JsonIgnore
    private User owner;

    private Double amount;

    private String status; // PROCESSING, PAID, FAILED

    private String razorpayPayoutId;

    private String utrNumber;

    private String bankName;

    private String accountNumber;

    private String transferMode;

    private String failureReason;

    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = "PROCESSING";
        }
    }
}

package com.billboarding.Entity.Payment;
import com.billboarding.Entity.Bookings.Booking;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_splits")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PaymentSplit {

    @Id @GeneratedValue
    private Long id;

    @OneToOne
    private Booking booking;

    private Double ownerAmount;
    private Double platformCommission;
    private Double gstAmount;

    private boolean settled; // Razorpay payout done?

    @Builder.Default
    private boolean refunded = false; // Was this split refunded?

    private LocalDateTime createdAt;
    private LocalDateTime settledAt;
    private LocalDateTime refundedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

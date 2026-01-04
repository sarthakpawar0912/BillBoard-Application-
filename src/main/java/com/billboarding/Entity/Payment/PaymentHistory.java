package com.billboarding.Entity.Payment;

import com.billboarding.ENUM.PaymentStatus;
import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Entity.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_history")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PaymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;

    private Double amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advertiser_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User advertiser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User owner;

    private LocalDateTime paidAt;

    private String razorpayRefundId;
    private Double refundAmount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus refundStatus;

    @PrePersist
    public void onPay() {
        paidAt = LocalDateTime.now();
    }
}

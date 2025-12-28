package com.billboarding.Entity.Payment;

import com.billboarding.ENUM.PaymentStatus;
import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Entity.User;
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

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "advertiser_id")
    private User advertiser;

    @ManyToOne
    @JoinColumn(name = "owner_id")
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

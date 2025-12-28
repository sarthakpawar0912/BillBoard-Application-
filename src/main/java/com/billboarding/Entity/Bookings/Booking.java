package com.billboarding.Entity.Bookings;

import com.billboarding.ENUM.BookingStatus;
import com.billboarding.ENUM.PaymentStatus;
import com.billboarding.Entity.OWNER.Billboard;
import com.billboarding.Entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "advertiser_id", nullable = false)
    private User advertiser;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private BookingStatus status;

    @ManyToOne
    @JoinColumn(name = "billboard_id", nullable = false)
    private Billboard billboard;

    private LocalDate startDate;
    private LocalDate endDate;

    private Double totalPrice;



    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private LocalDateTime createdAt;

    @Column(name = "rzp_order_id")
    private String razorpayOrderId;

    @Column(name = "rzp_payment_id")
    private String razorpayPaymentId;

    @Column(name = "rzp_signature")
    private String razorpaySignature;

    private LocalDateTime paymentDate;
    private String currency;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = BookingStatus.PENDING;
        if (paymentStatus == null) paymentStatus = PaymentStatus.NOT_PAID;
    }
}

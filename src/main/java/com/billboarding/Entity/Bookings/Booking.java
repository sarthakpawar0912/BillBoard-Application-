package com.billboarding.Entity.Bookings;

import com.billboarding.ENUM.BookingStatus;
import com.billboarding.ENUM.PaymentStatus;
import com.billboarding.Entity.Advertiser.Campaign;
import com.billboarding.Entity.OWNER.Billboard;
import com.billboarding.Entity.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings", indexes = {
    @Index(name = "idx_booking_advertiser", columnList = "advertiser_id"),
    @Index(name = "idx_booking_billboard", columnList = "billboard_id"),
    @Index(name = "idx_booking_status", columnList = "status"),
    @Index(name = "idx_booking_dates", columnList = "startDate, endDate"),
    @Index(name = "idx_booking_payment_status", columnList = "paymentStatus"),
    @Index(name = "idx_booking_created", columnList = "createdAt")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= 💰 PRICE BREAKDOWN =================

    // Original base amount (before discount, after smart pricing)
    private Double originalBaseAmount;

    // 🏷️ DISCOUNT (applied by owner, 0-50%)
    private Double discountPercent;    // e.g., 10.0 for 10%
    private Double discountAmount;     // Calculated: originalBaseAmount * discountPercent / 100

    // Base amount AFTER discount (this is what commission/GST is calculated on)
    private Double baseAmount;

    // Platform commission (calculated on baseAmount)
    private Double commissionAmount;

    // GST (calculated on baseAmount + commissionAmount)
    private Double gstAmount;

    // Final total payable
    private Double totalPrice;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advertiser_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User advertiser;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private BookingStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billboard_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Billboard billboard;

    private LocalDate startDate;
    private LocalDate endDate;

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

    // GST FIELDS
    private Double gstPercentage;  // e.g. 18

    // 🔒 COMMISSION % SNAPSHOT - locked at payment time
    // This ensures commission % is preserved even if admin changes it later
    private Double commissionPercent;  // e.g. 15.0 (stored at payment confirmation)

    // 🔒 PRICE PER DAY SNAPSHOT - stores billboard price at booking creation for audit
    private Double pricePerDayAtBooking;  // Original billboard pricePerDay

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Campaign campaign;


}

package com.billboarding.Entity.Payment;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Invoice meta
    private String invoiceNumber;
    private LocalDate invoiceDate;

    // Seller (Platform)
    private String sellerName;
    private String sellerGstin;
    private String sellerAddress;
    private String sellerState;
    private String sellerStateCode;  // e.g., "27" for Maharashtra

    // Buyer (Advertiser)
    private String buyerName;
    private String buyerEmail;
    private String buyerPhone;
    private String buyerGstin;
    private String buyerAddress;
    private String buyerState;
    private String buyerStateCode;

    // Booking reference
    private Long bookingId;
    private String billboardTitle;
    private String billboardLocation;

    private LocalDate startDate;
    private LocalDate endDate;
    private Long totalDays;

    // HSN/SAC Code (99833 for advertising services)
    private String sacCode;

    // ================= AMOUNT BREAKDOWN =================

    // Original base amount (before discount, after smart pricing)
    private Double originalBaseAmount;

    // 🏷️ DISCOUNT (applied by owner)
    private Double discountPercent;    // e.g., 10.0 for 10%
    private Double discountAmount;     // Calculated: originalBaseAmount * discountPercent / 100

    // Base amount AFTER discount (owner's rental)
    private Double baseAmount;

    // Platform commission (calculated on baseAmount)
    private Double commissionAmount;
    private Double commissionPercent;  // e.g., 15.0

    // Taxable value = baseAmount + commissionAmount
    private Double taxableValue;

    // GST breakdown
    private Double gstPercent;   // e.g., 18.0
    private Double cgstPercent;  // e.g., 9.0
    private Double sgstPercent;  // e.g., 9.0
    private Double igstPercent;  // e.g., 18.0 (for inter-state)

    private Double cgst;
    private Double sgst;
    private Double igst;

    private Double totalGst;     // cgst + sgst OR igst
    private Double totalAmount;  // taxableValue + totalGst

    private String currency; // INR

    // Payment reference
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private LocalDateTime paymentDate;

    // Metadata
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (sacCode == null) sacCode = "998365";  // Advertising services SAC code
        if (currency == null) currency = "INR";
    }
}

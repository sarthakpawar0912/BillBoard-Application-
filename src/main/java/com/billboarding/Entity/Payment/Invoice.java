package com.billboarding.Entity.Payment;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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

    // Seller (Owner / Platform)
    private String sellerName;
    private String sellerGstin;
    private String sellerAddress;

    // Buyer (Advertiser)
    private String buyerName;
    private String buyerEmail;
    private String buyerGstin;

    // Booking reference
    private Long bookingId;
    private String billboardTitle;
    private String billboardLocation;

    private LocalDate startDate;
    private LocalDate endDate;
    private Long totalDays;

    // Amounts
    private Double baseAmount;

    private Double cgst;
    private Double sgst;
    private Double igst;

    private Double totalAmount;

    private String currency; // INR
}

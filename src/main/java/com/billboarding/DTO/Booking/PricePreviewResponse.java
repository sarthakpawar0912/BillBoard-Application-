package com.billboarding.DTO.Booking;

import lombok.*;

import java.time.LocalDate;

/**
 * Response DTO for price preview.
 * Contains complete price breakdown calculated by backend.
 *
 * CRITICAL: Frontend MUST use these values for display.
 * DO NOT recalculate on frontend.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricePreviewResponse {

    // Billboard info
    private Long billboardId;
    private String billboardTitle;
    private Double pricePerDay;

    // Booking dates
    private LocalDate startDate;
    private LocalDate endDate;
    private Long totalDays;

    // ================= PRICE BREAKDOWN =================

    // Base amount (after smart pricing - may include demand/weekend surge)
    private Double baseAmount;

    // Original base (before smart pricing adjustments)
    private Double originalBaseAmount;

    // Smart pricing adjustments applied (if any)
    private Boolean demandSurgeApplied;
    private Boolean weekendSurgeApplied;

    // Commission breakdown
    private Double commissionPercent;
    private Double commissionAmount;

    // GST breakdown
    private Double gstPercent;
    private Double gstAmount;
    private Double cgstPercent;
    private Double cgstAmount;
    private Double sgstPercent;
    private Double sgstAmount;

    // Taxable value (base + commission)
    private Double taxableValue;

    // ================= FINAL AMOUNT =================
    private Double totalAmount;

    private String currency;

    // ================= DISCOUNT PREVIEW =================
    // For future use when discount is applied
    private Double discountPercent;
    private Double discountAmount;
    private Double discountedBaseAmount;

    // Maximum allowed discount (for UI validation)
    private Double maxDiscountPercent;

    // ================= OWNER CONTACT (for negotiation) =================
    private String ownerName;
    private String ownerEmail;
    private String ownerPhone;
}

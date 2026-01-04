package com.billboarding.DTO.Booking;

import com.billboarding.ENUM.BookingStatus;
import com.billboarding.ENUM.PaymentStatus;
import com.billboarding.Entity.Bookings.Booking;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Booking response DTO that hides sensitive user data (passwords)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponseDTO {

    private Long id;

    // ================= PRICE BREAKDOWN =================

    // Original base amount (before discount)
    private Double originalBaseAmount;

    // 🏷️ DISCOUNT (applied by owner)
    private Double discountPercent;
    private Double discountAmount;

    // Base amount AFTER discount
    private Double baseAmount;

    // Commission & GST
    private Double commissionAmount;
    private Double gstAmount;
    private Double totalPrice;

    // Safe user info (no password)
    private UserSummaryDTO advertiser;

    private BookingStatus status;

    // Safe billboard info (owner without password)
    private BillboardSummaryDTO billboard;

    private LocalDate startDate;
    private LocalDate endDate;

    private PaymentStatus paymentStatus;
    private LocalDateTime createdAt;

    // Payment details
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private LocalDateTime paymentDate;
    private String currency;
    private Double gstPercentage;

    // 🔒 Locked values (for paid bookings - immutable after payment)
    private Double commissionPercent;      // Commission % used (locked at payment)
    private Double pricePerDayAtBooking;   // Billboard price when booking was created

    public static BookingResponseDTO fromEntity(Booking booking) {
        if (booking == null) return null;

        return BookingResponseDTO.builder()
                .id(booking.getId())
                // Price breakdown with discount
                .originalBaseAmount(booking.getOriginalBaseAmount())
                .discountPercent(booking.getDiscountPercent())
                .discountAmount(booking.getDiscountAmount())
                .baseAmount(booking.getBaseAmount())
                .commissionAmount(booking.getCommissionAmount())
                .gstAmount(booking.getGstAmount())
                .totalPrice(booking.getTotalPrice())
                // User & billboard info
                .advertiser(UserSummaryDTO.fromEntity(booking.getAdvertiser()))
                .status(booking.getStatus())
                .billboard(BillboardSummaryDTO.fromEntity(booking.getBillboard()))
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .paymentStatus(booking.getPaymentStatus())
                .createdAt(booking.getCreatedAt())
                .razorpayOrderId(booking.getRazorpayOrderId())
                .razorpayPaymentId(booking.getRazorpayPaymentId())
                .paymentDate(booking.getPaymentDate())
                .currency(booking.getCurrency())
                .gstPercentage(booking.getGstPercentage())
                .commissionPercent(booking.getCommissionPercent())
                .pricePerDayAtBooking(booking.getPricePerDayAtBooking())
                .build();
    }

    public static List<BookingResponseDTO> fromEntityList(List<Booking> bookings) {
        return bookings.stream()
                .map(BookingResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
}

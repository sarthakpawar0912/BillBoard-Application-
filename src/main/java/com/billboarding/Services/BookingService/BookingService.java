package com.billboarding.Services.BookingService;

import com.billboarding.DTO.Booking.PricePreviewResponse;
import com.billboarding.ENUM.BookingStatus;
import com.billboarding.ENUM.KycStatus;
import com.billboarding.ENUM.PaymentStatus;
import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Entity.OWNER.Billboard;
import com.billboarding.Entity.User;
import com.billboarding.Entity.Admin.PlatformSettings;
import com.billboarding.Repository.BillBoard.BillboardRepository;
import com.billboarding.Repository.Booking.BookingRepository;
import com.billboarding.Services.Audit.BookingAuditService;
import com.billboarding.Services.Admin.PlatformSettingsService;
import com.billboarding.Services.SmartPricing.SmartPricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BillboardRepository billboardRepository;
    private final BookingAuditService bookingAuditService;

    private final SmartPricingService smartPricingService;
    private final PlatformSettingsService platformSettingsService;

    // ================= CREATE BOOKING =================

    public Booking createBooking(
            User advertiser,
            Long billboardId,
            LocalDate startDate,
            LocalDate endDate
    ) {

        if (advertiser.getKycStatus() != KycStatus.APPROVED) {
            throw new RuntimeException("Your KYC must be APPROVED to book a billboard");
        }

        if (startDate == null || endDate == null) {
            throw new RuntimeException("Start date and end date must be provided");
        }
        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("End date cannot be before start date");
        }

        Billboard billboard = billboardRepository.findById(billboardId)
                .orElseThrow(() -> new RuntimeException("Billboard not found"));

        // 🔒 OVERLAP CHECK
        boolean conflict =
                bookingRepository.existsByBillboardAndStatusInAndEndDateGreaterThanEqualAndStartDateLessThanEqual(
                        billboard,
                        List.of(BookingStatus.PENDING, BookingStatus.APPROVED),
                        startDate,
                        endDate
                );

        if (conflict) {
            throw new RuntimeException("The billboard is already booked for the selected dates");
        }

        // ================= PRICE CALCULATION =================

        PlatformSettings settings = platformSettingsService.get();

        // 1️⃣ ORIGINAL BASE PRICE (SMART PRICING - before any discount)
        double originalBaseAmount =
                smartPricingService.calculateBasePrice(billboard, startDate, endDate);

        // 2️⃣ DISCOUNT (initially 0 - owner can apply later)
        double discountPercent = 0.0;
        double discountAmount = 0.0;

        // 3️⃣ BASE AMOUNT (after discount - same as original initially)
        double baseAmount = originalBaseAmount - discountAmount;

        // 4️⃣ COMMISSION (on discounted base)
        double commissionAmount =
                baseAmount * settings.getCommissionPercent() / 100;

        // 5️⃣ GST (ON BASE + COMMISSION)
        double gstAmount =
                (baseAmount + commissionAmount) * settings.getGstPercent() / 100;

        // 6️⃣ FINAL PAYABLE
        double totalAmount =
                baseAmount + commissionAmount + gstAmount;

        Booking booking = Booking.builder()
                .advertiser(advertiser)
                .billboard(billboard)
                .startDate(startDate)
                .endDate(endDate)
                // Price breakdown with discount support
                .originalBaseAmount(originalBaseAmount)  // 🔒 Store original for audit
                .discountPercent(discountPercent)        // 🏷️ Initially 0
                .discountAmount(discountAmount)          // 🏷️ Initially 0
                .baseAmount(baseAmount)                  // After discount
                .commissionAmount(commissionAmount)
                .gstAmount(gstAmount)
                .totalPrice(totalAmount)
                .gstPercentage(settings.getGstPercent())
                .pricePerDayAtBooking(billboard.getPricePerDay())  // 🔒 Snapshot original price for audit
                .status(BookingStatus.PENDING)
                .paymentStatus(PaymentStatus.NOT_PAID)
                .currency(settings.getCurrency())
                .build();

        booking = bookingRepository.save(booking);

        bookingAuditService.log(booking, "CREATED", advertiser);

        return booking;
    }

    // ================= ADVERTISER =================

    public List<Booking> getMyBookings(User advertiser) {
        // Use findByAdvertiserWithDetails to ensure billboard and price data loads correctly
        return bookingRepository.findByAdvertiserWithDetails(advertiser);
    }

    public Booking cancelMyBooking(User advertiser, Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getAdvertiser().getId().equals(advertiser.getId())) {
            throw new RuntimeException("You can only cancel your own bookings");
        }

        // Allow cancellation for PENDING or APPROVED (before payment)
        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.APPROVED) {
            throw new RuntimeException("This booking cannot be cancelled");
        }

        // If already paid, cannot use this endpoint
        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            throw new RuntimeException("Cannot cancel a paid booking. Use cancel-after-payment endpoint");
        }

        booking.setStatus(BookingStatus.CANCELLED);

        bookingAuditService.log(booking, "CANCELLED", advertiser);

        return bookingRepository.save(booking);
    }

    // ================= ADMIN =================

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> getBookingsByStatus(BookingStatus status) {
        return bookingRepository.findByStatus(status);
    }

    public Booking approveBooking(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Only PENDING bookings can be approved");
        }

        booking.setStatus(BookingStatus.APPROVED);
        booking.setPaymentStatus(PaymentStatus.PENDING);

        bookingAuditService.log(booking, "APPROVED", null);

        return bookingRepository.save(booking);
    }

    public Booking rejectBooking(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Only PENDING bookings can be rejected");
        }

        booking.setStatus(BookingStatus.REJECTED);
        booking.setPaymentStatus(PaymentStatus.FAILED);

        bookingAuditService.log(booking, "REJECTED", null);

        return bookingRepository.save(booking);
    }

    // ================= CANCEL AFTER PAYMENT =================

    public Booking cancelAfterPayment(User advertiser, Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getAdvertiser().getId().equals(advertiser.getId())) {
            throw new RuntimeException("You can cancel only your booking");
        }

        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            booking.setStatus(BookingStatus.CANCELLED_NO_REFUND);
            bookingAuditService.log(booking, "CANCELLED_NO_REFUND", advertiser);
        } else {
            booking.setStatus(BookingStatus.CANCELLED);
            bookingAuditService.log(booking, "CANCELLED", advertiser);
        }

        return bookingRepository.save(booking);
    }

    // ================= PRICE RECALCULATION (for unpaid bookings) =================

    /**
     * Recalculates prices for unpaid bookings using CURRENT billboard price and commission %.
     * This ensures price updates by owner/admin apply to pending/approved (unpaid) requests.
     *
     * RULES:
     * - Only recalculates if payment status is NOT_PAID (not PENDING/PAID)
     * - PAID bookings are NEVER recalculated (prices are locked at payment)
     * - If Razorpay order already exists, DON'T recalculate (order amount is fixed)
     * - Called before payment order creation to ensure latest price
     */
    public Booking recalculatePricesIfUnpaid(Booking booking) {

        // CRITICAL: Never recalculate paid bookings - prices are locked
        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            return booking;
        }

        // CRITICAL: If Razorpay order already exists, DON'T recalculate
        // The order has a fixed amount and recalculating would cause mismatch
        if (booking.getRazorpayOrderId() != null) {
            return booking;
        }

        // Only recalculate for PENDING or APPROVED status (active unpaid bookings)
        if (booking.getStatus() != BookingStatus.PENDING &&
            booking.getStatus() != BookingStatus.APPROVED) {
            return booking;
        }

        Billboard billboard = booking.getBillboard();
        if (billboard == null) {
            throw new RuntimeException("Billboard not found for booking #" + booking.getId());
        }

        PlatformSettings settings = platformSettingsService.get();

        // 1️⃣ Recalculate ORIGINAL BASE PRICE using CURRENT billboard.pricePerDay
        double newOriginalBaseAmount = smartPricingService.calculateBasePrice(
                billboard,
                booking.getStartDate(),
                booking.getEndDate()
        );

        // 2️⃣ Preserve existing discount (if any)
        double discountPercent = booking.getDiscountPercent() != null ? booking.getDiscountPercent() : 0.0;
        double discountAmount = newOriginalBaseAmount * discountPercent / 100;

        // 3️⃣ Calculate BASE AMOUNT after discount
        double newBaseAmount = newOriginalBaseAmount - discountAmount;

        // 4️⃣ Recalculate COMMISSION using CURRENT settings.commissionPercent
        double newCommissionAmount = newBaseAmount * settings.getCommissionPercent() / 100;

        // 5️⃣ Recalculate GST
        double newGstAmount = (newBaseAmount + newCommissionAmount) * settings.getGstPercent() / 100;

        // 6️⃣ Recalculate TOTAL
        double newTotalPrice = newBaseAmount + newCommissionAmount + newGstAmount;

        // Check if prices actually changed
        boolean priceChanged = !approximatelyEqual(booking.getBaseAmount(), newBaseAmount) ||
                               !approximatelyEqual(booking.getCommissionAmount(), newCommissionAmount) ||
                               !approximatelyEqual(booking.getTotalPrice(), newTotalPrice);

        if (priceChanged) {
            System.out.println("[BookingService] Recalculating prices for booking #" + booking.getId());
            System.out.println("  Old: base=" + booking.getBaseAmount() + ", total=" + booking.getTotalPrice());
            System.out.println("  New: base=" + newBaseAmount + ", total=" + newTotalPrice);
            System.out.println("  Discount: " + discountPercent + "% = " + discountAmount);

            booking.setOriginalBaseAmount(newOriginalBaseAmount);
            booking.setDiscountPercent(discountPercent);
            booking.setDiscountAmount(discountAmount);
            booking.setBaseAmount(newBaseAmount);
            booking.setCommissionAmount(newCommissionAmount);
            booking.setGstAmount(newGstAmount);
            booking.setTotalPrice(newTotalPrice);
            booking.setGstPercentage(settings.getGstPercent());

            booking = bookingRepository.save(booking);
            bookingAuditService.log(booking, "PRICE_RECALCULATED", null);
        }

        return booking;
    }

    /**
     * Helper to compare doubles with tolerance (avoid floating point issues)
     */
    private boolean approximatelyEqual(Double a, Double b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return Math.abs(a - b) < 0.01;
    }

    /**
     * Get booking by ID with full details (for payment flow)
     */
    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    // ================= DISCOUNT MANAGEMENT =================

    /**
     * Apply discount to a booking (Owner action).
     * Can only be applied to PENDING/APPROVED unpaid bookings.
     *
     * @param bookingId       The booking to apply discount to
     * @param discountPercent Discount percentage (0-50)
     * @param owner           The owner applying the discount
     * @return Updated booking with discount applied
     */
    public Booking applyDiscount(Long bookingId, Double discountPercent, User owner) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Validate owner ownership
        if (!booking.getBillboard().getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("You can only apply discount to your own billboard bookings");
        }

        // Cannot apply discount to paid bookings
        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            throw new RuntimeException("Cannot apply discount to paid bookings");
        }

        // Only PENDING or APPROVED bookings can have discount applied
        if (booking.getStatus() != BookingStatus.PENDING &&
            booking.getStatus() != BookingStatus.APPROVED) {
            throw new RuntimeException("Discount can only be applied to PENDING or APPROVED bookings");
        }

        // Validate discount range
        if (discountPercent == null || discountPercent < 0) {
            discountPercent = 0.0;
        }

        // Determine max discount (50% weekdays, 30% weekends)
        boolean isWeekend = booking.getStartDate().getDayOfWeek() == DayOfWeek.SATURDAY ||
                           booking.getStartDate().getDayOfWeek() == DayOfWeek.SUNDAY;
        double maxDiscount = isWeekend ? 30.0 : 50.0;

        if (discountPercent > maxDiscount) {
            throw new RuntimeException("Discount cannot exceed " + maxDiscount + "% for this booking");
        }

        PlatformSettings settings = platformSettingsService.get();

        // Get original base amount (recalculate if null for legacy bookings)
        double originalBase = booking.getOriginalBaseAmount() != null
                ? booking.getOriginalBaseAmount()
                : smartPricingService.calculateBasePrice(
                        booking.getBillboard(),
                        booking.getStartDate(),
                        booking.getEndDate()
                  );

        // Calculate discount
        double discountAmount = originalBase * discountPercent / 100;
        double newBaseAmount = originalBase - discountAmount;

        // Recalculate commission on discounted base
        double commissionAmount = newBaseAmount * settings.getCommissionPercent() / 100;

        // Recalculate GST
        double gstAmount = (newBaseAmount + commissionAmount) * settings.getGstPercent() / 100;

        // Recalculate total
        double totalPrice = newBaseAmount + commissionAmount + gstAmount;

        // Apply changes
        booking.setOriginalBaseAmount(originalBase);
        booking.setDiscountPercent(discountPercent);
        booking.setDiscountAmount(discountAmount);
        booking.setBaseAmount(newBaseAmount);
        booking.setCommissionAmount(commissionAmount);
        booking.setGstAmount(gstAmount);
        booking.setTotalPrice(totalPrice);
        booking.setGstPercentage(settings.getGstPercent());

        booking = bookingRepository.save(booking);

        String action = discountPercent > 0
                ? "DISCOUNT_APPLIED_" + discountPercent + "%"
                : "DISCOUNT_REMOVED";
        bookingAuditService.log(booking, action, owner);

        System.out.println("[BookingService] Discount applied to booking #" + bookingId +
                           ": " + discountPercent + "% = " + discountAmount);

        return booking;
    }

    // ================= PRICE PREVIEW (SINGLE SOURCE OF TRUTH) =================

    /**
     * Calculate price preview for a potential booking.
     * This is the AUTHORITATIVE source for price calculation.
     * Frontend MUST use this to display prices before booking creation.
     *
     * @param billboardId The billboard to book
     * @param startDate   Booking start date
     * @param endDate     Booking end date
     * @return Complete price breakdown
     */
    public PricePreviewResponse calculatePricePreview(Long billboardId, LocalDate startDate, LocalDate endDate) {

        // Validate dates
        if (startDate == null || endDate == null) {
            throw new RuntimeException("Start date and end date are required");
        }
        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("End date cannot be before start date");
        }

        Billboard billboard = billboardRepository.findById(billboardId)
                .orElseThrow(() -> new RuntimeException("Billboard not found"));

        PlatformSettings settings = platformSettingsService.get();

        // Calculate number of days
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        // Original base (simple calculation)
        double originalBase = billboard.getPricePerDay() * days;

        // Smart pricing base (with demand/weekend surge)
        double baseAmount = smartPricingService.calculateBasePrice(billboard, startDate, endDate);

        // Detect which surges were applied
        boolean demandSurgeApplied = baseAmount > (originalBase * 1.1); // More than 10% increase suggests demand surge
        boolean weekendSurgeApplied = startDate.getDayOfWeek() == DayOfWeek.SATURDAY ||
                                      startDate.getDayOfWeek() == DayOfWeek.SUNDAY;

        // Commission calculation
        double commissionPercent = settings.getCommissionPercent();
        double commissionAmount = baseAmount * commissionPercent / 100;

        // Taxable value (base + commission)
        double taxableValue = baseAmount + commissionAmount;

        // GST calculation
        double gstPercent = settings.getGstPercent();
        double gstAmount = taxableValue * gstPercent / 100;
        double cgstPercent = gstPercent / 2;
        double sgstPercent = gstPercent / 2;
        double cgstAmount = gstAmount / 2;
        double sgstAmount = gstAmount / 2;

        // Total
        double totalAmount = baseAmount + commissionAmount + gstAmount;

        // Maximum discount allowed (50% for weekdays, 30% for weekends)
        double maxDiscountPercent = weekendSurgeApplied ? 30.0 : 50.0;

        // Get owner contact for negotiation
        User owner = billboard.getOwner();

        return PricePreviewResponse.builder()
                .billboardId(billboard.getId())
                .billboardTitle(billboard.getTitle())
                .pricePerDay(billboard.getPricePerDay())
                .startDate(startDate)
                .endDate(endDate)
                .totalDays(days)
                // Price breakdown
                .originalBaseAmount(originalBase)
                .baseAmount(baseAmount)
                .demandSurgeApplied(demandSurgeApplied)
                .weekendSurgeApplied(weekendSurgeApplied)
                // Commission
                .commissionPercent(commissionPercent)
                .commissionAmount(commissionAmount)
                // GST
                .gstPercent(gstPercent)
                .gstAmount(gstAmount)
                .cgstPercent(cgstPercent)
                .cgstAmount(cgstAmount)
                .sgstPercent(sgstPercent)
                .sgstAmount(sgstAmount)
                .taxableValue(taxableValue)
                // Total
                .totalAmount(totalAmount)
                .currency(settings.getCurrency())
                // Discount (initially 0)
                .discountPercent(0.0)
                .discountAmount(0.0)
                .discountedBaseAmount(baseAmount)
                .maxDiscountPercent(maxDiscountPercent)
                // Owner contact (for negotiation)
                .ownerName(owner != null ? owner.getName() : null)
                .ownerEmail(owner != null ? owner.getEmail() : null)
                .ownerPhone(owner != null ? owner.getPhone() : null)
                .build();
    }

    /**
     * Calculate price preview with discount applied.
     * Used when owner applies a discount to the booking.
     *
     * @param billboardId     The billboard ID
     * @param startDate       Booking start date
     * @param endDate         Booking end date
     * @param discountPercent Discount percentage (0-50)
     * @return Complete price breakdown with discount
     */
    public PricePreviewResponse calculatePricePreviewWithDiscount(
            Long billboardId,
            LocalDate startDate,
            LocalDate endDate,
            Double discountPercent
    ) {
        // Get base preview
        PricePreviewResponse preview = calculatePricePreview(billboardId, startDate, endDate);

        // Validate discount
        if (discountPercent == null || discountPercent < 0) {
            discountPercent = 0.0;
        }
        if (discountPercent > preview.getMaxDiscountPercent()) {
            throw new RuntimeException("Discount cannot exceed " + preview.getMaxDiscountPercent() + "%");
        }

        // Apply discount to base amount (BEFORE commission/GST)
        double discountAmount = preview.getBaseAmount() * discountPercent / 100;
        double discountedBase = preview.getBaseAmount() - discountAmount;

        // Recalculate commission on discounted base
        double commissionAmount = discountedBase * preview.getCommissionPercent() / 100;

        // Recalculate taxable value
        double taxableValue = discountedBase + commissionAmount;

        // Recalculate GST
        double gstAmount = taxableValue * preview.getGstPercent() / 100;
        double cgstAmount = gstAmount / 2;
        double sgstAmount = gstAmount / 2;

        // Recalculate total
        double totalAmount = discountedBase + commissionAmount + gstAmount;

        // Update preview with discounted values
        preview.setDiscountPercent(discountPercent);
        preview.setDiscountAmount(discountAmount);
        preview.setDiscountedBaseAmount(discountedBase);
        preview.setCommissionAmount(commissionAmount);
        preview.setTaxableValue(taxableValue);
        preview.setGstAmount(gstAmount);
        preview.setCgstAmount(cgstAmount);
        preview.setSgstAmount(sgstAmount);
        preview.setTotalAmount(totalAmount);

        return preview;
    }
}

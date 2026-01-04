package com.billboarding.Services.Payment;

import com.billboarding.Entity.Advertiser.AdvertiserProfile;
import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Entity.Payment.Invoice;
import com.billboarding.Entity.User;
import com.billboarding.Entity.Admin.PlatformSettings;
import com.billboarding.Repository.Advertiser.AdvertiserProfileRepository;
import com.billboarding.Repository.Booking.BookingRepository;
import com.billboarding.Repository.Payment.InvoiceRepository;
import com.billboarding.Services.Admin.PlatformSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final BookingRepository bookingRepository;
    private final InvoiceRepository invoiceRepository;
    private final AdvertiserProfileRepository advertiserProfileRepository;
    private final PlatformSettingsService platformSettingsService;

    // Invoice number counter (in production, use database sequence)
    private static final AtomicLong invoiceCounter = new AtomicLong(1000);

    /**
     * Generate GST invoice after successful payment
     * Idempotent - returns existing invoice if already generated
     */
    public Invoice generateInvoice(Long bookingId) {

        // ========== DEFENSIVE VALIDATION ==========
        if (bookingId == null) {
            throw new IllegalArgumentException("Booking ID is required to generate invoice");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + bookingId));

        // IDEMPOTENCY: Return existing invoice if already generated
        Optional<Invoice> existing = invoiceRepository.findByBookingId(bookingId);
        if (existing.isPresent()) {
            return existing.get();
        }

        // Validate payment status - only generate invoice for PAID bookings
        if (booking.getPaymentStatus() != com.billboarding.ENUM.PaymentStatus.PAID) {
            throw new RuntimeException("Invoice can only be generated for paid bookings. Current status: " + booking.getPaymentStatus());
        }

        // Validate booking has valid amounts
        if (booking.getTotalPrice() == null || booking.getTotalPrice() <= 0) {
            throw new RuntimeException("Cannot generate invoice: booking has invalid total amount");
        }

        if (booking.getBaseAmount() == null || booking.getBaseAmount() <= 0) {
            throw new RuntimeException("Cannot generate invoice: base amount is missing or invalid");
        }

        User advertiser = booking.getAdvertiser();
        if (advertiser == null) {
            throw new RuntimeException("Cannot generate invoice: advertiser information is missing");
        }

        PlatformSettings settings = platformSettingsService.get();

        // Get advertiser profile for company details
        Optional<AdvertiserProfile> profileOpt = advertiserProfileRepository.findByAdvertiser(advertiser);
        String companyName = profileOpt.map(AdvertiserProfile::getCompanyName)
                .filter(name -> name != null && !name.isBlank())
                .orElse(advertiser.getName());

        // Calculate number of days
        long days = java.time.temporal.ChronoUnit.DAYS.between(
                booking.getStartDate(),
                booking.getEndDate()
        ) + 1;

        // ================= AMOUNT CALCULATIONS =================
        // These values come from the booking entity (already calculated during booking creation)

        // Original base amount (before discount)
        double originalBaseAmount = booking.getOriginalBaseAmount() != null
                ? booking.getOriginalBaseAmount()
                : booking.getBaseAmount(); // Fallback for legacy bookings

        // Discount (if any)
        double discountPercent = booking.getDiscountPercent() != null
                ? booking.getDiscountPercent()
                : 0.0;
        double discountAmount = booking.getDiscountAmount() != null
                ? booking.getDiscountAmount()
                : 0.0;

        // Base amount AFTER discount (owner's rental amount)
        double baseAmount = booking.getBaseAmount();
        double commissionAmount = booking.getCommissionAmount(); // Platform commission
        double gstAmount = booking.getGstAmount();             // GST on taxable value
        double totalAmount = booking.getTotalPrice();          // Final total

        // Taxable value = base + commission (GST is calculated on this)
        double taxableValue = baseAmount + commissionAmount;

        // Commission percentage - USE LOCKED VALUE from booking, fallback to calculation
        // CRITICAL: booking.commissionPercent is set at payment time and should be used for invoices
        double commissionPercent;
        if (booking.getCommissionPercent() != null) {
            // Use the locked commission % from payment time
            commissionPercent = booking.getCommissionPercent();
        } else if (baseAmount > 0) {
            // Fallback: calculate from amounts (for legacy bookings without commissionPercent)
            commissionPercent = (commissionAmount / baseAmount) * 100;
        } else {
            // Last resort: use current settings
            commissionPercent = settings.getCommissionPercent() != null
                    ? settings.getCommissionPercent() : 15.0;
        }

        // GST percentage (from settings)
        double gstPercent = settings.getGstPercent() != null
                ? settings.getGstPercent()
                : 18.0;

        // Split GST into CGST and SGST (intra-state) - 9% each for 18% GST
        // For inter-state, use IGST instead
        double cgstPercent = gstPercent / 2;  // 9%
        double sgstPercent = gstPercent / 2;  // 9%
        double cgstAmount = gstAmount / 2;
        double sgstAmount = gstAmount / 2;
        double igstAmount = 0.0;  // For intra-state, IGST is 0

        // Generate unique invoice number: INV-YYYYMMDD-XXXX
        String invoiceNumber = generateInvoiceNumber();

        Invoice invoice = Invoice.builder()
                // Invoice Meta
                .invoiceNumber(invoiceNumber)
                .invoiceDate(LocalDate.now())

                // Seller (Platform) - Registered in Maharashtra
                .sellerName(settings.getPlatformName() != null
                        ? settings.getPlatformName()
                        : "Billboard & Hoarding Pvt Ltd")
                .sellerGstin("27ABCDE1234F1Z5")  // Maharashtra GSTIN
                .sellerAddress("123 Business Park, Andheri East, Mumbai - 400069")
                .sellerState("Maharashtra")
                .sellerStateCode("27")

                // Buyer (Advertiser)
                .buyerName(companyName)
                .buyerEmail(advertiser.getEmail())
                .buyerPhone(advertiser.getPhone())
                .buyerGstin(null)  // Advertiser GSTIN (optional, can be added to profile)
                .buyerAddress(null) // Can be fetched from profile if available
                .buyerState("Maharashtra")  // Default, ideally from profile
                .buyerStateCode("27")

                // Booking Reference
                .bookingId(booking.getId())
                .billboardTitle(booking.getBillboard().getTitle())
                .billboardLocation(booking.getBillboard().getLocation())
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .totalDays(days)

                // SAC Code for Advertising Services
                .sacCode("998365")

                // ================= AMOUNT BREAKDOWN =================

                // Original base (before discount)
                .originalBaseAmount(originalBaseAmount)

                // Discount (if any)
                .discountPercent(discountPercent)
                .discountAmount(discountAmount)

                // Base amount AFTER discount (owner's rental)
                .baseAmount(baseAmount)

                // Platform commission (separate line item for transparency)
                .commissionAmount(commissionAmount)
                .commissionPercent(commissionPercent)

                // Taxable value (base + commission)
                .taxableValue(taxableValue)

                // GST breakdown
                .gstPercent(gstPercent)
                .cgstPercent(cgstPercent)
                .sgstPercent(sgstPercent)
                .cgst(cgstAmount)
                .sgst(sgstAmount)
                .igstPercent(0.0)
                .igst(igstAmount)
                .totalGst(gstAmount)

                // Final Total
                .totalAmount(totalAmount)
                .currency(booking.getCurrency() != null ? booking.getCurrency() : "INR")

                // Payment Reference
                .razorpayOrderId(booking.getRazorpayOrderId())
                .razorpayPaymentId(booking.getRazorpayPaymentId())
                .paymentDate(booking.getPaymentDate())

                .build();

        Invoice savedInvoice = invoiceRepository.save(invoice);
        System.out.println("[InvoiceService] Generated invoice " + invoiceNumber + " for booking #" + bookingId);

        return savedInvoice;
    }

    /**
     * Generate unique invoice number in format: INV-YYYYMMDD-XXXX
     */
    private String generateInvoiceNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long counter = invoiceCounter.incrementAndGet();
        return String.format("INV-%s-%04d", datePart, counter % 10000);
    }

    /**
     * Get invoice by booking ID
     */
    public Optional<Invoice> getInvoiceByBookingId(Long bookingId) {
        return invoiceRepository.findByBookingId(bookingId);
    }
}

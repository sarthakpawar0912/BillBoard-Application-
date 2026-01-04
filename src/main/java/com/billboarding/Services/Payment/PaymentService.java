package com.billboarding.Services.Payment;

import com.billboarding.DTO.Payment.CreatePaymentOrderRequest;
import com.billboarding.DTO.Payment.PaymentOrderResponse;
import com.billboarding.DTO.Payment.VerifyPaymentRequest;
import com.billboarding.ENUM.BookingStatus;
import com.billboarding.ENUM.PaymentStatus;
import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Entity.Payment.PaymentHistory;
import com.billboarding.Entity.Payment.PaymentSplit;
import com.billboarding.Entity.User;
import com.billboarding.Repository.Booking.BookingRepository;
import com.billboarding.Repository.Payment.PaymentHistoryRepository;
import com.billboarding.Repository.Payment.PaymentSplitRepository;
import com.billboarding.Services.Admin.Wallet.AdminWalletService;
import com.billboarding.Services.Owner.Wallet.OwnerWalletService;
import com.billboarding.Services.BookingService.BookingService;
import com.billboarding.Services.Admin.PlatformSettingsService;
import com.billboarding.Entity.Admin.PlatformSettings;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentHistoryRepository paymentRepo;
    private final PaymentSplitService paymentSplitService;
    private final PaymentSplitRepository paymentSplitRepository;
    private final OwnerWalletService ownerWalletService;
    private final AdminWalletService adminWalletService;
    private final InvoiceService invoiceService;
    private final BookingService bookingService;
    private final PlatformSettingsService platformSettingsService;

    @Value("${razorpay.key_id}")
    private String keyId;

    @Value("${razorpay.key_secret}")
    private String keySecret;

    private RazorpayClient razorpay;

    @PostConstruct
    public void initClient() throws RazorpayException {
        this.razorpay = new RazorpayClient(keyId, keySecret);
    }

    /**
     * 1️⃣ Create Razorpay order for a given booking
     */
    @Transactional
    public PaymentOrderResponse createOrder(CreatePaymentOrderRequest req, User advertiser) {

        // ========== DEFENSIVE VALIDATION ==========
        if (req == null || req.getBookingId() == null) {
            throw new IllegalArgumentException("Booking ID is required to create payment order");
        }

        if (advertiser == null) {
            throw new IllegalArgumentException("User authentication is required");
        }

        Booking booking = bookingRepository.findById(req.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + req.getBookingId()));

        if (!booking.getAdvertiser().getId().equals(advertiser.getId())) {
            throw new RuntimeException("You are not authorized to pay for this booking");
        }

        // Validate booking status - only APPROVED bookings can be paid
        if (booking.getStatus() != BookingStatus.APPROVED) {
            throw new RuntimeException("Only approved bookings can be paid. Current status: " + booking.getStatus());
        }

        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            throw new RuntimeException("This booking has already been paid");
        }

        // Validate booking has valid amounts
        if (booking.getTotalPrice() == null || booking.getTotalPrice() <= 0) {
            throw new RuntimeException("Invalid booking amount. Please contact support.");
        }

        // 🔄 RECALCULATE PRICES: Apply latest billboard price & commission % for unpaid bookings
        // This ensures price changes by owner/admin apply until payment is completed
        booking = bookingService.recalculatePricesIfUnpaid(booking);

        // IDEMPOTENCY: If order already exists AND prices haven't changed, return existing order
        // BUT if prices changed, we need to create a new order with updated amount
        if (booking.getRazorpayOrderId() != null && booking.getPaymentStatus() == PaymentStatus.PENDING) {
            System.out.println("[PaymentService] Returning existing order for booking #" + booking.getId());
            return PaymentOrderResponse.builder()
                    .orderId(booking.getRazorpayOrderId())
                    .keyId(keyId)
                    .bookingId(booking.getId())
                    .amount(booking.getTotalPrice())
                    .currency("INR")
                    .receipt("BOOKING_" + booking.getId())
                    .build();
        }

        try {
            long amountPaise = Math.round(booking.getTotalPrice() * 100);

            JSONObject options = new JSONObject();
            options.put("amount", amountPaise);
            options.put("currency", "INR");
            options.put("receipt", "BOOKING_" + booking.getId());
            options.put("payment_capture", 1);

            Order order = razorpay.orders.create(options);

            booking.setRazorpayOrderId(order.get("id"));
            booking.setPaymentStatus(PaymentStatus.PENDING);
            booking.setCurrency("INR");
            bookingRepository.save(booking);

            System.out.println("[PaymentService] Created Razorpay order: " + order.get("id") + " for booking #" + booking.getId());

            return PaymentOrderResponse.builder()
                    .orderId(order.get("id"))
                    .keyId(keyId)
                    .bookingId(booking.getId())
                    .amount(booking.getTotalPrice())
                    .currency("INR")
                    .receipt("BOOKING_" + booking.getId())
                    .build();

        } catch (Exception e) {
            System.err.println("[PaymentService] Error creating order: " + e.getMessage());
            throw new RuntimeException("Failed to create payment order: " + e.getMessage());
        }
    }

/**
     * 2️⃣ Verify Razorpay signature and mark booking as PAID
     *    Also record PaymentHistory
     */
    @Transactional
    public Booking verifyAndCapture(VerifyPaymentRequest req, User advertiser) {

        // ========== DEFENSIVE VALIDATION ==========
        if (req == null) {
            throw new IllegalArgumentException("Payment verification request is required");
        }

        if (req.getRazorpayOrderId() == null || req.getRazorpayOrderId().isBlank()) {
            throw new IllegalArgumentException("Razorpay Order ID is required for payment verification");
        }

        if (req.getRazorpayPaymentId() == null || req.getRazorpayPaymentId().isBlank()) {
            throw new IllegalArgumentException("Razorpay Payment ID is required for payment verification");
        }

        if (req.getRazorpaySignature() == null || req.getRazorpaySignature().isBlank()) {
            throw new IllegalArgumentException("Razorpay Signature is required for payment verification");
        }

        if (advertiser == null) {
            throw new IllegalArgumentException("User authentication is required");
        }

        Booking booking = bookingRepository.findByRazorpayOrderId(req.getRazorpayOrderId())
                .orElseThrow(() -> new RuntimeException("Booking not found for order: " + req.getRazorpayOrderId()));

        // safety: ensure same advertiser
        if (!booking.getAdvertiser().getId().equals(advertiser.getId())) {
            throw new RuntimeException("Not allowed to verify payment for someone else's booking");
        }

        // IDEMPOTENCY CHECK: If already PAID, return early (prevents duplicate processing)
        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            System.out.println("[PaymentService] Booking #" + booking.getId() + " already PAID. Skipping duplicate verify.");
            return booking;
        }

        boolean valid = isSignatureValid(
                req.getRazorpayOrderId(),
                req.getRazorpayPaymentId(),
                req.getRazorpaySignature()
        );

        if (!valid) {
            booking.setPaymentStatus(PaymentStatus.FAILED);
            bookingRepository.save(booking);
            System.err.println("[PaymentService] Signature verification FAILED for order: " + req.getRazorpayOrderId());
            throw new RuntimeException("Invalid Razorpay signature. Payment verification failed.");
        }

        System.out.println("[PaymentService] Signature verified for order: " + req.getRazorpayOrderId());

        // 🔒 LOCK COMMISSION % AT PAYMENT TIME
        // This ensures the commission % used is preserved even if admin changes it later
        PlatformSettings settings = platformSettingsService.get();
        if (booking.getCommissionPercent() == null) {
            booking.setCommissionPercent(settings.getCommissionPercent());
            System.out.println("[PaymentService] Locked commission % at: " + settings.getCommissionPercent() + "%");
        }

        // mark booking as PAID
        booking.setRazorpayPaymentId(req.getRazorpayPaymentId());
        booking.setRazorpaySignature(req.getRazorpaySignature());
        booking.setPaymentStatus(PaymentStatus.PAID);
        booking.setPaymentDate(LocalDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);

        // Check if PaymentHistory already exists (idempotency for history)
        boolean historyExists = paymentRepo.findByRazorpayPaymentId(req.getRazorpayPaymentId()).isPresent();
        if (!historyExists) {
            // record in payment_history table
            PaymentHistory history = PaymentHistory.builder()
                    .razorpayOrderId(req.getRazorpayOrderId())
                    .razorpayPaymentId(req.getRazorpayPaymentId())
                    .razorpaySignature(req.getRazorpaySignature())
                    .booking(savedBooking)
                    .advertiser(savedBooking.getAdvertiser())
                    .owner(savedBooking.getBillboard().getOwner())
                    .amount(savedBooking.getTotalPrice())
                    .build();

            paymentRepo.save(history);
            System.out.println("[PaymentService] PaymentHistory created for payment: " + req.getRazorpayPaymentId());
        } else {
            System.out.println("[PaymentService] PaymentHistory already exists for payment: " + req.getRazorpayPaymentId());
        }

        // CRITICAL: Create payment split and credit wallets (idempotent - checks internally)
        // This credits Owner Wallet with baseAmount and Admin Wallet with commission
        paymentSplitService.createSplit(savedBooking);

        // AUTO-GENERATE GST INVOICE (idempotent - won't duplicate if already exists)
        try {
            invoiceService.generateInvoice(savedBooking.getId());
            System.out.println("[PaymentService] Invoice auto-generated for booking #" + savedBooking.getId());
        } catch (Exception e) {
            // Log but don't fail payment - invoice can be generated later
            System.err.println("[PaymentService] Warning: Failed to auto-generate invoice: " + e.getMessage());
        }

        System.out.println("[PaymentService] Payment verified and processed successfully for booking #" + savedBooking.getId());

        return savedBooking;
    }

    /**
     * 3️⃣ Refund payment via Razorpay
     */
    /* ================= REFUND (🔥 FIXED) ================= */

    @Transactional
    public void initiateRefund(Long bookingId, User advertiser) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getAdvertiser().getId().equals(advertiser.getId())) {
            throw new RuntimeException("You can refund only your own booking");
        }

        if (booking.getPaymentStatus() != PaymentStatus.PAID) {
            throw new RuntimeException("Only PAID bookings can be refunded");
        }

        PaymentHistory history = paymentRepo.findByBooking_Id(bookingId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Payment record not found"));

        // 🚫 Prevent duplicate refund
        if (history.getRefundStatus() != null) {
            throw new RuntimeException("Refund already initiated or completed");
        }

        try {
            // ✅ FULL REFUND (NO AMOUNT)
            razorpay.payments.refund(history.getRazorpayPaymentId());

            history.setRefundStatus(PaymentStatus.PENDING);
            history.setRefundAmount(booking.getTotalPrice());
            paymentRepo.save(history);

            // REVERSE WALLET CREDITS - Deduct from Owner and Admin wallets
            reverseWalletCredits(booking);

            // Update booking status
            booking.setPaymentStatus(PaymentStatus.REFUNDED);
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);

        } catch (RazorpayException e) {
            throw new RuntimeException("Refund failed: " + e.getMessage(), e);
        }
    }

    /**
     * 4️⃣ Reverse wallet credits when refund is processed
     */
    @Transactional
    public void reverseWalletCredits(Booking booking) {
        // Get payment split to know the amounts credited
        PaymentSplit split = paymentSplitRepository.findByBooking(booking)
                .orElseThrow(() -> new RuntimeException("Payment split not found for booking"));

        User owner = booking.getBillboard().getOwner();

        // Deduct base amount from Owner Wallet
        try {
            ownerWalletService.debit(
                    owner,
                    split.getOwnerAmount(),
                    "REFUND#BOOKING#" + booking.getId()
            );
        } catch (RuntimeException e) {
            // Log but continue - owner may have already withdrawn
            System.err.println("Warning: Could not debit owner wallet for refund: " + e.getMessage());
        }

        // Deduct commission from Admin Wallet
        try {
            adminWalletService.debitForRefund(booking, split.getPlatformCommission());
        } catch (RuntimeException e) {
            // Log but continue
            System.err.println("Warning: Could not debit admin wallet for refund: " + e.getMessage());
        }

        // Mark the split as reversed/refunded
        split.setRefunded(true);
        split.setRefundedAt(LocalDateTime.now());
        paymentSplitRepository.save(split);
    }

    /**
     * 5️⃣ Process refund webhook from Razorpay (called when refund is completed)
     */
    @Transactional
    public void processRefundWebhook(String paymentId, String refundId, Double refundAmount) {
        PaymentHistory history = paymentRepo.findByRazorpayPaymentId(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        history.setRefundStatus(PaymentStatus.REFUNDED);
        history.setRazorpayRefundId(refundId);
        history.setRefundAmount(refundAmount);
        paymentRepo.save(history);

        // Update booking status
        Booking booking = history.getBooking();
        booking.setPaymentStatus(PaymentStatus.REFUNDED);
        bookingRepository.save(booking);
    }



/**
     * Utility: verify HMAC SHA256 signature
     */
    private boolean isSignatureValid(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);

            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String computedSignature = bytesToHex(digest);

            return computedSignature.equalsIgnoreCase(signature);
        } catch (Exception e) {
            return false;
        }
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) sb.append('0');
            sb.append(hex);
        }
        return sb.toString();
    }

}
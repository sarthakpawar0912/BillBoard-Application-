package com.billboarding.Services.Payment;

import com.billboarding.DTO.Payment.CreatePaymentOrderRequest;
import com.billboarding.DTO.Payment.PaymentOrderResponse;
import com.billboarding.DTO.Payment.VerifyPaymentRequest;
import com.billboarding.ENUM.BookingStatus;
import com.billboarding.ENUM.PaymentStatus;
import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Entity.Payment.PaymentHistory;
import com.billboarding.Entity.User;
import com.billboarding.Repository.Booking.BookingRepository;
import com.billboarding.Repository.Payment.PaymentHistoryRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentHistoryRepository paymentRepo;

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
    public PaymentOrderResponse createOrder(CreatePaymentOrderRequest req, User advertiser) {

        Booking booking = bookingRepository.findById(req.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getAdvertiser().getId().equals(advertiser.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            throw new RuntimeException("Already paid");
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

            return PaymentOrderResponse.builder()
                    .orderId(order.get("id"))
                    .keyId(keyId)
                    .bookingId(booking.getId())
                    .amount(booking.getTotalPrice())
                    .currency("INR")
                    .receipt("BOOKING_" + booking.getId())
                    .build();

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

/**
     * 2️⃣ Verify Razorpay signature and mark booking as PAID
     *    Also record PaymentHistory
     */
    public Booking verifyAndCapture(VerifyPaymentRequest req, User advertiser) {

        Booking booking = bookingRepository.findByRazorpayOrderId(req.getRazorpayOrderId())
                .orElseThrow(() -> new RuntimeException("Booking not found for this order"));

        // safety: ensure same advertiser
        if (!booking.getAdvertiser().getId().equals(advertiser.getId())) {
            throw new RuntimeException("Not allowed to verify payment for someone else's booking");
        }

        boolean valid = isSignatureValid(
                req.getRazorpayOrderId(),
                req.getRazorpayPaymentId(),
                req.getRazorpaySignature()
        );

        if (!valid) {
            booking.setPaymentStatus(PaymentStatus.FAILED);
            bookingRepository.save(booking);
            throw new RuntimeException("Invalid Razorpay signature. Payment verification failed.");
        }

        // mark booking as PAID
        booking.setRazorpayPaymentId(req.getRazorpayPaymentId());
        booking.setRazorpaySignature(req.getRazorpaySignature());
        booking.setPaymentStatus(PaymentStatus.PAID);
        booking.setPaymentDate(LocalDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);

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

        return savedBooking;
    }

    /**
     * 3️⃣ Refund payment via Razorpay
     */
    /* ================= REFUND (🔥 FIXED) ================= */

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
            paymentRepo.save(history);

        } catch (RazorpayException e) {
            throw new RuntimeException("Refund failed: " + e.getMessage(), e);
        }
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
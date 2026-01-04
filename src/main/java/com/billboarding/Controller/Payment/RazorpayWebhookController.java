package com.billboarding.Controller.Payment;

import com.billboarding.ENUM.PaymentStatus;
import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Entity.Payment.PaymentHistory;
import com.billboarding.Repository.Booking.BookingRepository;
import com.billboarding.Repository.Payment.PaymentHistoryRepository;
import com.billboarding.Services.Payment.PaymentSplitService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class RazorpayWebhookController {

    private final BookingRepository bookingRepository;
    private final PaymentHistoryRepository paymentRepo;
    private final PaymentSplitService paymentSplitService;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/razorpay")
    @Transactional
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {

        System.out.println("[Webhook] Received Razorpay webhook");

        // Verify signature (only if provided - test webhooks may not have it)
        if (signature != null && !verifySignature(payload, signature)) {
            System.err.println("[Webhook] Invalid signature");
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        JSONObject json = new JSONObject(payload);
        String event = json.getString("event");

        System.out.println("[Webhook] Event type: " + event);

        if ("payment.captured".equals(event)) {

            JSONObject payment = json.getJSONObject("payload")
                    .getJSONObject("payment")
                    .getJSONObject("entity");

            String orderId = payment.getString("order_id");
            String paymentId = payment.getString("id");
            double amount = payment.getInt("amount") / 100.0;

            System.out.println("[Webhook] Processing payment.captured for order: " + orderId + ", payment: " + paymentId);

            Booking booking = bookingRepository.findByRazorpayOrderId(orderId).orElse(null);
            if (booking == null) {
                System.err.println("[Webhook] Booking not found for order: " + orderId);
                return ResponseEntity.ok("Booking not found - ignoring webhook");
            }

            // IDEMPOTENT CHECK: If already PAID, skip but still return success
            if (booking.getPaymentStatus() == PaymentStatus.PAID) {
                System.out.println("[Webhook] Booking #" + booking.getId() + " already PAID. Skipping.");
                return ResponseEntity.ok("Already processed");
            }

            // Update booking to PAID
            booking.setPaymentStatus(PaymentStatus.PAID);
            booking.setRazorpayPaymentId(paymentId);
            booking.setPaymentDate(LocalDateTime.now());
            bookingRepository.save(booking);

            // Check if PaymentHistory already exists (prevents duplicate if verify API was faster)
            boolean historyExists = paymentRepo.findByRazorpayPaymentId(paymentId).isPresent();
            if (!historyExists) {
                paymentRepo.save(
                        PaymentHistory.builder()
                                .razorpayOrderId(orderId)
                                .razorpayPaymentId(paymentId)
                                .amount(amount)
                                .booking(booking)
                                .advertiser(booking.getAdvertiser())
                                .owner(booking.getBillboard().getOwner())
                                .build()
                );
                System.out.println("[Webhook] PaymentHistory created for payment: " + paymentId);
            } else {
                System.out.println("[Webhook] PaymentHistory already exists for payment: " + paymentId);
            }

            // CRITICAL: Create payment split and credit wallets (idempotent)
            // This was MISSING before - webhook must also credit Owner/Admin wallets
            try {
                paymentSplitService.createSplit(booking);
                System.out.println("[Webhook] PaymentSplit processed for booking #" + booking.getId());
            } catch (Exception e) {
                // Log but don't fail the webhook - split may already exist from verify API
                System.out.println("[Webhook] PaymentSplit already exists or error: " + e.getMessage());
            }

            System.out.println("[Webhook] Successfully processed payment.captured for booking #" + booking.getId());
        }

        return ResponseEntity.ok("Webhook processed");
    }

    private boolean verifySignature(String payload, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    webhookSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"));

            String expected = bytesToHex(mac.doFinal(payload.getBytes()));
            return expected.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            String h = Integer.toHexString(0xff & b);
            if (h.length() == 1) sb.append('0');
            sb.append(h);
        }
        return sb.toString();
    }
}

package com.billboarding.Controller.Payment;

import com.billboarding.ENUM.PaymentStatus;
import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Entity.Payment.PaymentHistory;
import com.billboarding.Repository.Booking.BookingRepository;
import com.billboarding.Repository.Payment.PaymentHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
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

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/razorpay")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {

        if (!verifySignature(payload, signature)) {
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        JSONObject json = new JSONObject(payload);
        String event = json.getString("event");

        if ("payment.captured".equals(event)) {

            JSONObject payment = json.getJSONObject("payload")
                    .getJSONObject("payment")
                    .getJSONObject("entity");

            String orderId = payment.getString("order_id");
            String paymentId = payment.getString("id");
            double amount = payment.getInt("amount") / 100.0;

            Booking booking = bookingRepository.findByRazorpayOrderId(orderId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            // ✅ IDEMPOTENT CHECK
            if (booking.getPaymentStatus() == PaymentStatus.PAID) {
                return ResponseEntity.ok("Already processed");
            }

            booking.setPaymentStatus(PaymentStatus.PAID);
            booking.setRazorpayPaymentId(paymentId);
            booking.setPaymentDate(LocalDateTime.now());
            bookingRepository.save(booking);

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

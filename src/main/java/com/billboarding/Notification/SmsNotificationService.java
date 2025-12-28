package com.billboarding.Notification;

import com.billboarding.Entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Handles SMS notifications (mock implementation).
 */
@Slf4j
@Service
public class SmsNotificationService {

    /**
     * Generic SMS sender (Mock)
     * Prints SMS message to logs.
     */
    public void sendSms(String phone, String message) {

        if (phone == null || phone.isBlank()) {
            log.warn("⚠ Cannot send SMS — Phone number is missing");
            return;
        }

        log.info("📱 Sending SMS to {}: {}", phone, message);
    }

    // ------------------ SMS Templates ------------------ //

    public void sendRegistrationSms(User user) {
        sendSms(user.getPhone(),
                "Welcome " + user.getName() + "! Registration successful 🎉");
    }

    public void sendBookingSms(User user, Long bookingId) {
        sendSms(user.getPhone(),
                "Your booking is confirmed! Booking ID: " + bookingId);
    }

    public void sendPaymentSms(User user, Long paymentId) {
        sendSms(user.getPhone(),
                "Payment received successfully! Payment ID: " + paymentId);
    }

    public void sendCancellationSms(User user, Long bookingId) {
        sendSms(user.getPhone(),
                "Your booking has been cancelled. ID: " + bookingId);
    }

    public void sendReminderSms(User user, String message) {
        sendSms(user.getPhone(),
                "Reminder: " + message);
    }
}

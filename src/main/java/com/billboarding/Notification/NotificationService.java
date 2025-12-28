package com.billboarding.Notification;


import com.billboarding.Entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Central Notification service.
 * Right now: Email is primary, SMS is optional/mock.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailNotificationService emailNotificationService;
    private final SmsNotificationService smsNotificationService; // mock (logs only)

    // 🔹 Registration notification (Email + mock SMS)
    public void sendRegistrationNotification(User user) {
        emailNotificationService.sendRegistrationEmail(user);
        smsNotificationService.sendRegistrationSms(user); // only logs, no cost
    }

    // 🔹 Booking notification
    public void sendBookingNotification(User user, Long bookingId) {
        emailNotificationService.sendBookingEmail(user, bookingId);
        smsNotificationService.sendBookingSms(user, bookingId); // mock
    }

    // 🔹 Payment notification
    public void sendPaymentNotification(User user, Long paymentId) {
        emailNotificationService.sendPaymentEmail(user, paymentId);
        smsNotificationService.sendPaymentSms(user, paymentId); // mock
    }

    // 🔹 Cancellation notification
    public void sendCancellationNotification(User user, Long bookingId) {
        emailNotificationService.sendCancellationEmail(user, bookingId);
        smsNotificationService.sendCancellationSms(user, bookingId); // mock
    }

    // 🔹 Reminder notification
    public void sendReminderNotification(User user, String message) {
        emailNotificationService.sendReminderEmail(user, message);
        smsNotificationService.sendReminderSms(user, message); // mock
    }
}
package com.billboarding.Notification;

  // your correct User model
import com.billboarding.Entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Handles sending EMAIL notifications for all events.
 */
@Slf4j                       // Lombok → enables log.info(), log.error() etc.
@Service                     // Marks class as Spring-managed service
@RequiredArgsConstructor     // Lombok → auto-creates constructor for final fields
public class EmailNotificationService {

    private final JavaMailSender mailSender;   // Spring's email sending component

    @Value("${spring.mail.from}")              // Reads variable from application.properties
    private String fromEmail;

    /**
     * Generic email sending method reused by all notification types.
     */
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);      // sender address (your Gmail)
            msg.setTo(to);               // receiver email
            msg.setSubject(subject);     // email subject line
            msg.setText(body);           // email content

            mailSender.send(msg);
            log.info("📧 Email sent to {}", to);

        } catch (Exception e) {
            log.error("❌ Failed to send email: {}", e.getMessage());
        }
    }

    // ------------ EMAIL TEMPLATES ------------ //

    public void sendRegistrationEmail(User user) {
        String body =
                "Dear " + user.getName() + ",\n\n" +
                        "Welcome to Billboarding! Your registration was successful.\n\n" +
                        "Best Regards,\nBillboarding Team";

        sendEmail(user.getEmail(), "Welcome to Billboarding 🎉", body);
    }

    public void sendBookingEmail(User user, Long bookingId) {
        String body =
                "Dear " + user.getName() + ",\n\n" +
                        "Your booking (ID: " + bookingId + ") has been confirmed.\n\n" +
                        "Thank you!\nBillboarding Team";

        sendEmail(user.getEmail(), "Booking Confirmed ✔", body);
    }

    public void sendPaymentEmail(User user, Long paymentId) {
        String body =
                "Dear " + user.getName() + ",\n\n" +
                        "Your payment (ID: " + paymentId + ") was successful.\n\n" +
                        "Thank you!\nBillboarding Team";

        sendEmail(user.getEmail(), "Payment Successful 💰", body);
    }

    public void sendCancellationEmail(User user, Long bookingId) {
        String body =
                "Dear " + user.getName() + ",\n\n" +
                        "Your booking (ID: " + bookingId + ") has been cancelled.\n\n" +
                        "Regards,\nBillboarding Team";

        sendEmail(user.getEmail(), "Booking Cancelled ❌", body);
    }

    public void sendReminderEmail(User user, String message) {
        String body =
                "Dear " + user.getName() + ",\n\n" +
                        message + "\n\n" +
                        "Regards,\nBillboarding Team";

        sendEmail(user.getEmail(), "Reminder ⏰", body);
    }

    public void sendKycApprovedEmail(User user){
        String subject="KYC Approved";
        String body="Hello"+user.getName()+",\n\n"+
                "Your KYC has been approved. You can now access all features of our platform.\n\n"+
                "Best Regards,\nBillboarding Team";
        sendEmail(user.getEmail(),subject,body);
    }

    public void sendKycRejectedEmail(User user, String reason) {
        String subject = "KYC Rejected";
        String body = "Hello " + user.getName() + ",\n\nYour KYC was rejected.\nReason: " + reason;
        sendEmail(user.getEmail(), subject, body);
    }
}

package com.billboarding.Services.Audit;

import com.billboarding.DTO.ADMIN.BookingAuditResponse;
import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Entity.Bookings.BookingAudit;
import com.billboarding.Entity.User;
import com.billboarding.Repository.Audit.BookingAuditRepository;
import com.billboarding.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingAuditService {

    private final BookingAuditRepository auditRepo;
    private final UserRepository userRepository;

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Log an audit entry for a booking action.
     */
    public void log(
            Booking booking,
            String action,
            User actor
    ) {
        BookingAudit audit = BookingAudit.builder()
                .bookingId(booking.getId())
                .action(action)
                .actorRole(actor.getRole().name())
                .actorId(actor.getId())
                .build();

        auditRepo.save(audit);
    }

    /**
     * Get formatted audit trail for a booking.
     * Returns data in the format expected by the frontend.
     */
    public BookingAuditResponse getAuditTrail(Long bookingId) {
        List<BookingAudit> audits = auditRepo.findByBookingId(bookingId);

        List<BookingAuditResponse.BookingAuditEntry> history = audits.stream()
                .map(this::mapToEntry)
                .collect(Collectors.toList());

        return BookingAuditResponse.builder()
                .bookingId(bookingId)
                .history(history)
                .build();
    }

    /**
     * Map a BookingAudit entity to a BookingAuditEntry DTO.
     */
    private BookingAuditResponse.BookingAuditEntry mapToEntry(BookingAudit audit) {
        // Get user name for performedBy field
        String performedBy = getPerformedByName(audit.getActorId(), audit.getActorRole());

        // Format timestamp
        String timestamp = audit.getCreatedAt() != null
                ? audit.getCreatedAt().format(ISO_FORMATTER)
                : "";

        // Generate contextual details based on action
        String details = generateActionDetails(audit.getAction(), audit.getActorRole());

        return BookingAuditResponse.BookingAuditEntry.builder()
                .action(audit.getAction())
                .timestamp(timestamp)
                .performedBy(performedBy)
                .details(details)
                .build();
    }

    /**
     * Get a human-readable name for who performed the action.
     */
    private String getPerformedByName(Long actorId, String actorRole) {
        if (actorId == null) {
            return actorRole + " (System)";
        }

        return userRepository.findById(actorId)
                .map(user -> user.getName() + " (" + actorRole + ")")
                .orElse(actorRole + " #" + actorId);
    }

    /**
     * Generate contextual details for an action.
     */
    private String generateActionDetails(String action, String actorRole) {
        switch (action.toUpperCase()) {
            case "CREATED":
                return "Booking request submitted";
            case "APPROVED":
                return "Booking approved by " + actorRole.toLowerCase();
            case "REJECTED":
                return "Booking rejected by " + actorRole.toLowerCase();
            case "CANCELLED":
                return "Booking cancelled";
            case "CANCELLED_NO_REFUND":
                return "Booking cancelled (no refund - late cancellation)";
            case "PAID":
                return "Payment completed successfully";
            case "PAYMENT_VERIFIED":
                return "Payment verification successful";
            case "PAYMENT_FAILED":
                return "Payment verification failed";
            case "COMPLETED":
                return "Booking campaign completed";
            case "STATUS_CHANGED":
                return "Booking status updated";
            default:
                return null;
        }
    }
}

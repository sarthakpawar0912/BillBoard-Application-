package com.billboarding.DTO.ADMIN;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for Booking Audit Trail response.
 * Matches the frontend interface for proper data binding.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingAuditResponse {

    private Long bookingId;
    private List<BookingAuditEntry> history;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingAuditEntry {
        private String action;      // CREATED, APPROVED, REJECTED, CANCELLED, PAID, etc.
        private String timestamp;   // ISO 8601 formatted timestamp
        private String performedBy; // Name or identifier of who performed the action
        private String details;     // Optional additional context
    }
}

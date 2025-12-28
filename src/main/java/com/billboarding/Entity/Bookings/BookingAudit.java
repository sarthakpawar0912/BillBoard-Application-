package com.billboarding.Entity.Bookings;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Entity
@Table(name = "booking_audit_log")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long bookingId;
    private String action;     // CREATED, APPROVED, PAID, CANCELLED_NO_REFUND
    private String actorRole;  // ADMIN / OWNER / ADVERTISER
    private Long actorId;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

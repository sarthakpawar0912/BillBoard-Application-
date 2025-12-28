package com.billboarding.Services.Audit;

import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Entity.Bookings.BookingAudit;
import com.billboarding.Entity.User;
import com.billboarding.Repository.Audit.BookingAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingAuditService {

    private final BookingAuditRepository auditRepo;

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
}

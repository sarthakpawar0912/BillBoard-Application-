package com.billboarding.Repository.Audit;

import com.billboarding.Entity.Bookings.BookingAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingAuditRepository extends JpaRepository<BookingAudit, Long> {

    List<BookingAudit> findByBookingId(Long bookingId);
}

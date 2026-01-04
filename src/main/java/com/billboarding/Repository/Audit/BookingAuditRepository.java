package com.billboarding.Repository.Audit;

import com.billboarding.Entity.Bookings.BookingAudit;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookingAuditRepository extends JpaRepository<BookingAudit, Long> {

    List<BookingAudit> findByBookingId(Long bookingId);

    @Modifying
    @Transactional
    @Query("DELETE FROM BookingAudit ba WHERE ba.bookingId IN :bookingIds")
    void deleteByBookingIdIn(List<Long> bookingIds);
}

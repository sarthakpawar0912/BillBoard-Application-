package com.billboarding.Repository.Payment;
import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Entity.Payment.PaymentSplit;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
public interface PaymentSplitRepository
        extends JpaRepository<PaymentSplit, Long> {

    Optional<PaymentSplit> findByBooking(Booking booking);

    @Modifying
    @Transactional
    @Query("DELETE FROM PaymentSplit ps WHERE ps.booking.id IN :bookingIds")
    void deleteByBookingIdIn(List<Long> bookingIds);
}

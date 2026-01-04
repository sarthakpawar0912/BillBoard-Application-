package com.billboarding.Repository.Payment;

import com.billboarding.Entity.Payment.PaymentHistory;
import com.billboarding.Entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {

    List<PaymentHistory> findByAdvertiser(User advertiser);

    List<PaymentHistory> findByOwner(User owner);

    List<PaymentHistory> findByBooking_Id(Long bookingId);

    Optional<PaymentHistory> findByRazorpayPaymentId(String razorpayPaymentId);

    @Modifying
    @Transactional
    @Query("DELETE FROM PaymentHistory ph WHERE ph.booking.id IN :bookingIds")
    void deleteByBookingIdIn(List<Long> bookingIds);
}
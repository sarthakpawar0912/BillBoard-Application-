package com.billboarding.Repository.Payment;

import com.billboarding.Entity.Payment.PaymentHistory;
import com.billboarding.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {

    List<PaymentHistory> findByAdvertiser(User advertiser);

    List<PaymentHistory> findByOwner(User owner);

    List<PaymentHistory> findByBooking_Id(Long bookingId);

    Optional<PaymentHistory> findByRazorpayPaymentId(String razorpayPaymentId);

}
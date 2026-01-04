package com.billboarding.Repository.Payment;

import com.billboarding.Entity.Payment.Invoice;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByBookingId(Long bookingId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Invoice i WHERE i.bookingId IN :bookingIds")
    void deleteByBookingIdIn(List<Long> bookingIds);
}

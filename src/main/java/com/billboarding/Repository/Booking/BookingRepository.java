package com.billboarding.Repository.Booking;

import com.billboarding.ENUM.BookingStatus;
import com.billboarding.ENUM.PaymentStatus;
import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Entity.OWNER.Billboard;
import com.billboarding.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByAdvertiser(User advertiser);



    List<Booking> findByBillboard_OwnerAndStatus(User owner, BookingStatus status);


    List<Booking> findByBillboard_Owner(User owner);

    List<Booking> findByBillboard_Id(Long billboardId);

    // For admin → filter bookings by status
    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByStatusIn(List<BookingStatus> statuses);

    List<Booking> findByBillboard_OwnerAndStatusAndStartDateGreaterThanEqual(
            User owner, BookingStatus status, LocalDate date
    );

    List<Booking> findByBillboard_OwnerAndStatusAndEndDateLessThan(
            User owner, BookingStatus status, LocalDate date
    );

    Optional<Booking> findByRazorpayOrderId(String razorpayOrderId);


    boolean existsByBillboardAndStatusInAndEndDateGreaterThanEqualAndStartDateLessThanEqual(
            Billboard billboard,
            List<BookingStatus> statuses,
            LocalDate start,
            LocalDate end
    );

    long countByBillboardAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Billboard billboard,
            BookingStatus status,
            LocalDate end,
            LocalDate start
    );

    List<Booking> findByBillboard_IdAndStatusAndPaymentStatus(
            Long billboardId,
            BookingStatus status,
            PaymentStatus paymentStatus
    );



    List<Booking> findByBillboard_OwnerAndStatusAndPaymentStatus(
            User owner,
            BookingStatus status,
            PaymentStatus paymentStatus
    );

    @Query("""
SELECT COUNT(b)
FROM Booking b
WHERE b.billboard = :billboard
AND b.status = :status
AND :date BETWEEN b.startDate AND b.endDate
""")
    long countBookingsOnDate(
            @Param("billboard") Billboard billboard,
            @Param("status") BookingStatus status,
            @Param("date") LocalDate date
    );


}
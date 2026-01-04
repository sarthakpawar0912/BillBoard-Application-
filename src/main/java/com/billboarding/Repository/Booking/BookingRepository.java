package com.billboarding.Repository.Booking;

import com.billboarding.ENUM.BookingStatus;
import com.billboarding.ENUM.PaymentStatus;
import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Entity.OWNER.Billboard;
import com.billboarding.Entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByAdvertiser(User advertiser);

    // Fetch bookings with billboard, images and owner eagerly
    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.billboard bb LEFT JOIN FETCH bb.imagePaths LEFT JOIN FETCH bb.owner LEFT JOIN FETCH b.advertiser WHERE b.advertiser = :advertiser")
    List<Booking> findByAdvertiserWithDetails(@Param("advertiser") User advertiser);

    // Fetch bookings for owner with all details
    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.billboard bb LEFT JOIN FETCH bb.imagePaths LEFT JOIN FETCH bb.owner LEFT JOIN FETCH b.advertiser WHERE bb.owner = :owner")
    List<Booking> findByBillboardOwnerWithDetails(@Param("owner") User owner);

    // Fetch bookings for owner by status with all details
    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.billboard bb LEFT JOIN FETCH bb.imagePaths LEFT JOIN FETCH bb.owner LEFT JOIN FETCH b.advertiser WHERE bb.owner = :owner AND b.status = :status")
    List<Booking> findByBillboardOwnerAndStatusWithDetails(@Param("owner") User owner, @Param("status") BookingStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE Booking b SET b.campaign = NULL WHERE b.campaign.id = :campaignId")
    void clearCampaign(Long campaignId);

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


    List<Booking> findByCampaign_Id(Long campaignId);

    // ===== OPTIMIZED COUNT QUERIES =====
    long countByStatus(BookingStatus status);
    long countByPaymentStatus(PaymentStatus paymentStatus);
    long countByBillboard_Owner(User owner);

    // ===== REVENUE AGGREGATION (no entity loading) =====
    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.status = :status")
    Double sumTotalPriceByStatus(@Param("status") BookingStatus status);

    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.billboard.owner = :owner AND b.status = :status")
    Double sumTotalPriceByOwnerAndStatus(@Param("owner") User owner, @Param("status") BookingStatus status);

    @Query("SELECT COALESCE(SUM(b.commissionAmount), 0) FROM Booking b WHERE b.status = :status")
    Double sumCommissionByStatus(@Param("status") BookingStatus status);

    // ===== PAGINATED QUERIES =====
    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);
    Page<Booking> findByBillboard_Owner(User owner, Pageable pageable);
    Page<Booking> findByAdvertiser(User advertiser, Pageable pageable);
    Page<Booking> findByBillboard_OwnerAndStatus(User owner, BookingStatus status, Pageable pageable);

    @Query("SELECT DISTINCT b FROM Booking b " +
           "LEFT JOIN FETCH b.billboard bb " +
           "LEFT JOIN FETCH b.advertiser " +
           "WHERE bb.owner = :owner")
    Page<Booking> findByBillboardOwnerWithDetailsPaged(@Param("owner") User owner, Pageable pageable);

    // ===== ANALYTICS QUERIES (with JOIN FETCH for LAZY relationships) =====
    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.advertiser LEFT JOIN FETCH b.billboard")
    List<Booking> findAllWithDetails();

    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.advertiser LEFT JOIN FETCH b.billboard WHERE b.status = :status")
    List<Booking> findByStatusWithDetails(@Param("status") BookingStatus status);

    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.advertiser LEFT JOIN FETCH b.billboard WHERE b.status IN :statuses")
    List<Booking> findByStatusInWithDetails(@Param("statuses") List<BookingStatus> statuses);
}
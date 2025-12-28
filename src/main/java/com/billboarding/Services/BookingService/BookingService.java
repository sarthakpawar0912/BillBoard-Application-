package com.billboarding.Services.BookingService;

import com.billboarding.ENUM.BookingStatus;
import com.billboarding.ENUM.KycStatus;
import com.billboarding.ENUM.PaymentStatus;
import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Entity.OWNER.Billboard;
import com.billboarding.Entity.User;
import com.billboarding.Repository.BillBoard.BillboardRepository;
import com.billboarding.Repository.Booking.BookingRepository;
import com.billboarding.Services.Audit.BookingAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BillboardRepository billboardRepository;
    private final BookingAuditService bookingAuditService;

    public BookingService(BookingRepository bookingRepository, BillboardRepository billboardRepository, BookingAuditService bookingAuditService) {
        this.bookingRepository = bookingRepository;
        this.billboardRepository = billboardRepository;
        this.bookingAuditService = bookingAuditService;
    }

    public Booking createBooking(User advertiser,
                                 Long billboardId,
                                 LocalDate startDate,
                                 LocalDate endDate) {

        if (advertiser.getKycStatus() != KycStatus.APPROVED) {
            throw new RuntimeException("Your KYC must be APPROVED to book a billboard");
        }

        if (startDate == null || endDate == null) {
            throw new RuntimeException("Start date and end date must be provided");
        }
        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("End date cannot be before start date");
        }

        Billboard billboard = billboardRepository.findById(billboardId)
                .orElseThrow(() -> new RuntimeException("Billboard not found"));

        // ⭐ FIXED OVERLAP CHECK ⭐
        boolean conflict = bookingRepository
                .existsByBillboardAndStatusInAndEndDateGreaterThanEqualAndStartDateLessThanEqual(
                        billboard,
                        List.of(BookingStatus.PENDING, BookingStatus.APPROVED),
                        startDate,
                        endDate
                );

        if (conflict) {
            throw new RuntimeException("The billboard is already booked for the selected dates");
        }

        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        double totalPrice = billboard.getPricePerDay() * days;

        Booking booking = Booking.builder()
                .advertiser(advertiser)
                .billboard(billboard)
                .startDate(startDate)
                .endDate(endDate)
                .totalPrice(totalPrice)
                .status(BookingStatus.PENDING)
                .paymentStatus(PaymentStatus.NOT_PAID)
                .build();

        return bookingRepository.save(booking);
    }

    public List<Booking> getMyBookings(User advertiser) {
        return bookingRepository.findByAdvertiser(advertiser);
    }

    public Booking cancelMyBooking(User advertiser, Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getAdvertiser().getId().equals(advertiser.getId())) {
            throw new RuntimeException("You can only cancel your own bookings");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Only PENDING bookings can be cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }


    // ================= ADMIN OPERATIONS =================

    /**
     * 🔹 Admin: get ALL bookings in the system
     */
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    /**
     * 🔹 Admin: get bookings filtered by status
     * Example: PENDING / APPROVED / REJECTED / CANCELLED
     */
    public List<Booking> getBookingsByStatus(BookingStatus status) {
        return bookingRepository.findByStatus(status);
    }


    // ================= ADMIN ACTIONS =================

    public Booking approveBooking(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Only PENDING bookings can be approved");
        }

        booking.setStatus(BookingStatus.APPROVED);
        booking.setPaymentStatus(PaymentStatus.PENDING);  // advertiser must pay next

        return bookingRepository.save(booking);
    }

    public Booking rejectBooking(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Only PENDING bookings can be rejected");
        }

        booking.setStatus(BookingStatus.REJECTED);
        booking.setPaymentStatus(PaymentStatus.FAILED);

        return bookingRepository.save(booking);
    }

    public Booking cancelAfterPayment(User advertiser, Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getAdvertiser().getId().equals(advertiser.getId())) {
            throw new RuntimeException("You can cancel only your booking");
        }

        if (booking.getStatus() == BookingStatus.REJECTED) {
            throw new RuntimeException("Booking already rejected");
        }

        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            booking.setStatus(BookingStatus.CANCELLED_NO_REFUND);
        } else {
            booking.setStatus(BookingStatus.CANCELLED);
        }
        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            booking.setStatus(BookingStatus.CANCELLED_NO_REFUND);
            bookingAuditService.log(
                    booking,
                    "CANCELLED_NO_REFUND",
                    advertiser
            );
        } else {
            booking.setStatus(BookingStatus.CANCELLED);
            bookingAuditService.log(
                    booking,
                    "CANCELLED",
                    advertiser
            );
        }


        return bookingRepository.save(booking);
    }


}

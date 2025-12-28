package com.billboarding.Services.BookingService;

import com.billboarding.ENUM.BookingStatus;
import com.billboarding.ENUM.PaymentStatus;
import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Entity.User;
import com.billboarding.Repository.Booking.BookingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OwnerBooking {
    private final BookingRepository bookingRepository;

    public OwnerBooking(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public List<Booking> getBookingsForOwner(User owner) {
        return bookingRepository.findByBillboard_Owner(owner);
    }

    public Booking approveBooking(User owner, Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Owner can approve only his billboard bookings
        if (!booking.getBillboard().getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("You cannot approve bookings of another owner");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Only PENDING bookings can be approved");
        }

        booking.setStatus(BookingStatus.APPROVED);
        booking.setPaymentStatus(PaymentStatus.PENDING); // payment yet to come

        return bookingRepository.save(booking);
    }

    public Booking rejectBooking(User owner, Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getBillboard().getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("You cannot reject bookings of another owner");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Only PENDING bookings can be rejected");
        }

        booking.setStatus(BookingStatus.REJECTED);

        return bookingRepository.save(booking);
    }

    public List<Booking> getPendingRequests(User owner) {
        return bookingRepository.findByBillboard_OwnerAndStatus(
                owner,
                BookingStatus.PENDING
        );
    }

    public List<Booking> getUpcomingBookings(User owner) {
        return bookingRepository.findByBillboard_OwnerAndStatus(
                owner,
                BookingStatus.APPROVED
        );
    }

    public List<Booking> getCompletedBookings(User owner) {
        return bookingRepository.findByBillboard_OwnerAndStatus(
                owner,
                BookingStatus.APPROVED
        );
    }



}

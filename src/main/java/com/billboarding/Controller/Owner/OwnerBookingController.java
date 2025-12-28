package com.billboarding.Controller.Owner;

import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Entity.User;
import com.billboarding.Services.BookingService.OwnerBooking;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/owner/bookings")
public class OwnerBookingController {

    private final OwnerBooking ownerBooking;

    public OwnerBookingController(OwnerBooking ownerBooking) {
        this.ownerBooking = ownerBooking;
    }

    @GetMapping
    public ResponseEntity<List<Booking>> getOwnerBookings(Authentication auth) {
        User owner = (User) auth.getPrincipal();
        return ResponseEntity.ok(ownerBooking.getBookingsForOwner(owner));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Booking> approve(@PathVariable Long id, Authentication auth) {
        User owner = (User) auth.getPrincipal();
        return ResponseEntity.ok(ownerBooking.approveBooking(owner, id));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Booking> reject(@PathVariable Long id, Authentication auth) {
        User owner = (User) auth.getPrincipal();
        return ResponseEntity.ok(ownerBooking.rejectBooking(owner, id));
    }
    @GetMapping("/requests")
    public ResponseEntity<List<Booking>> pendingRequests(Authentication auth) {
        User owner = (User) auth.getPrincipal();
        return ResponseEntity.ok(ownerBooking.getPendingRequests(owner));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<Booking>> upcomingBookings(Authentication auth) {
        User owner = (User) auth.getPrincipal();
        return ResponseEntity.ok(ownerBooking.getUpcomingBookings(owner));
    }

    @GetMapping("/completed")
    public ResponseEntity<List<Booking>> completedBookings(Authentication auth) {
        User owner = (User) auth.getPrincipal();
        return ResponseEntity.ok(ownerBooking.getCompletedBookings(owner));
    }

}

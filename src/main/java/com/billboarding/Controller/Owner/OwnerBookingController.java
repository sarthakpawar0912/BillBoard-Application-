package com.billboarding.Controller.Owner;

import com.billboarding.Entity.Admin.PlatformSettings;
import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Entity.User;
import com.billboarding.Services.Admin.PlatformSettingsService;
import com.billboarding.Services.BookingService.BookingService;
import com.billboarding.Services.BookingService.OwnerBooking;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/owner/bookings")
public class OwnerBookingController {

    private final OwnerBooking ownerBooking;
    private final BookingService bookingService;
    private final PlatformSettingsService platformSettingsService;

    public OwnerBookingController(OwnerBooking ownerBooking, BookingService bookingService,
                                   PlatformSettingsService platformSettingsService) {
        this.ownerBooking = ownerBooking;
        this.bookingService = bookingService;
        this.platformSettingsService = platformSettingsService;
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

    // ================= DISCOUNT MANAGEMENT =================

    /**
     * Apply discount to a booking.
     * Only owner of the billboard can apply discount.
     * Discount range: 0-50% (weekdays), 0-30% (weekends)
     *
     * POST /api/owner/bookings/{id}/discount?percent=10
     */
    @PostMapping("/{id}/discount")
    public ResponseEntity<Booking> applyDiscount(
            @PathVariable Long id,
            @RequestParam Double percent,
            Authentication auth
    ) {
        User owner = (User) auth.getPrincipal();
        Booking updatedBooking = bookingService.applyDiscount(id, percent, owner);
        return ResponseEntity.ok(updatedBooking);
    }

    /**
     * Remove discount from a booking (set to 0%).
     *
     * DELETE /api/owner/bookings/{id}/discount
     */
    @DeleteMapping("/{id}/discount")
    public ResponseEntity<Booking> removeDiscount(
            @PathVariable Long id,
            Authentication auth
    ) {
        User owner = (User) auth.getPrincipal();
        Booking updatedBooking = bookingService.applyDiscount(id, 0.0, owner);
        return ResponseEntity.ok(updatedBooking);
    }

    /**
     * Get discount limits for a booking.
     * Returns max discount % based on weekday/weekend.
     *
     * GET /api/owner/bookings/{id}/discount-limits
     */
    @GetMapping("/{id}/discount-limits")
    public ResponseEntity<Map<String, Object>> getDiscountLimits(
            @PathVariable Long id,
            Authentication auth
    ) {
        User owner = (User) auth.getPrincipal();
        Booking booking = bookingService.getBookingById(id);

        // Verify ownership
        if (!booking.getBillboard().getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("You can only view discount limits for your own billboard bookings");
        }

        boolean isWeekend = booking.getStartDate().getDayOfWeek().getValue() >= 6;
        double maxDiscount = isWeekend ? 30.0 : 50.0;

        // Get current platform settings for commission and GST
        PlatformSettings settings = platformSettingsService.get();

        Map<String, Object> limits = new HashMap<>();
        limits.put("bookingId", id);
        limits.put("isWeekend", isWeekend);
        limits.put("maxDiscountPercent", maxDiscount);
        limits.put("currentDiscountPercent", booking.getDiscountPercent() != null ? booking.getDiscountPercent() : 0.0);
        limits.put("currentDiscountAmount", booking.getDiscountAmount() != null ? booking.getDiscountAmount() : 0.0);
        limits.put("originalBaseAmount", booking.getOriginalBaseAmount());
        limits.put("currentTotal", booking.getTotalPrice());
        // Include commission and GST percentages for accurate frontend estimation
        limits.put("commissionPercent", settings.getCommissionPercent());
        limits.put("gstPercent", settings.getGstPercent());

        return ResponseEntity.ok(limits);
    }

}

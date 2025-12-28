package com.billboarding.Controller.Admin;

import com.billboarding.ENUM.BookingStatus;
import com.billboarding.ENUM.KycStatus;
import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Entity.User;
import com.billboarding.Repository.Audit.BookingAuditRepository;
import com.billboarding.Services.Admin.AdminAnalyticsService;
import com.billboarding.Services.Admin.AdminsService;
import com.billboarding.Services.AdminService;
import com.billboarding.Services.Audit.BookingAuditService;
import com.billboarding.Services.BookingService.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin APIs:
 *  - View all users
 *  - Manage KYC (approve / reject)
 *  - Block / unblock users
 *  - View all bookings (optionally filter by status)
 *
 * All endpoints are protected in SecurityConfig:
 *    /api/admin/** → only ADMIN role can access.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminsService adminService;
    private final BookingService bookingService;
    private final AdminAnalyticsService adminAnalyticsService;
    private final BookingAuditRepository bookingAuditRepository;

    public AdminController(AdminsService adminService, BookingService bookingService, AdminService adminAnalyticsService, AdminAnalyticsService adminAnalyticsService1, BookingAuditService bookingAuditService, BookingAuditRepository bookingAuditRepository) {
        this.adminService = adminService;
        this.bookingService = bookingService;
        this.adminAnalyticsService = adminAnalyticsService1;
        this.bookingAuditRepository = bookingAuditRepository;
    }

    /**
     * Simple text dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<String> dashboard() {
        return ResponseEntity.ok("Admin dashboard: manage users, KYC, and bookings");
    }

    /**
     * 1️⃣ Get all users
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    /**
     * 2️⃣ Get only users with PENDING KYC
     */
    @GetMapping("/users/pending-kyc")
    public ResponseEntity<List<User>> getPendingKycUsers() {
        return ResponseEntity.ok(adminService.getPendingKycUsers());
    }

    /**
     * 3️⃣ Approve KYC for a user
     */
    @PostMapping("/users/{id}/kyc-approve")
    public ResponseEntity<User> approveKyc(@PathVariable Long id) {
        User updated = adminService.updateKycStatus(id, KycStatus.APPROVED);
        return ResponseEntity.ok(updated);
    }

    /**
     * 4️⃣ Reject KYC for a user
     */
    @PostMapping("/users/{id}/kyc-reject")
    public ResponseEntity<User> rejectKyc(@PathVariable Long id) {
        User updated = adminService.updateKycStatus(id, KycStatus.REJECTED);
        return ResponseEntity.ok(updated);
    }

    /**
     * 5️⃣ Block a user (cannot log in)
     */
    @PostMapping("/users/{id}/block")
    public ResponseEntity<User> blockUser(@PathVariable Long id) {
        User updated = adminService.blockUser(id);
        return ResponseEntity.ok(updated);
    }

    /**
     * 6️⃣ Unblock a user
     */
    @PostMapping("/users/{id}/unblock")
    public ResponseEntity<User> unblockUser(@PathVariable Long id) {
        User updated = adminService.unblockUser(id);
        return ResponseEntity.ok(updated);
    }

    /**
     * 7️⃣ View all bookings OR filter by status
     *    /api/admin/bookings
     *    /api/admin/bookings?status=APPROVED
     */
    @GetMapping("/bookings")
    public ResponseEntity<List<Booking>> getBookings(
            @RequestParam(name = "status", required = false) BookingStatus status
    ) {
        if (status != null) {
            return ResponseEntity.ok(bookingService.getBookingsByStatus(status));
        }
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    /**
     * 8️⃣ Approve a booking
     */
    @PostMapping("/bookings/{id}/approve")
    public ResponseEntity<Booking> approveBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.approveBooking(id));
    }

    /**
     * 9️⃣ Reject a booking
     */
    @PostMapping("/bookings/{id}/reject")
    public ResponseEntity<Booking> rejectBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.rejectBooking(id));
    }

    @GetMapping("/analytics/cancellations")
    public ResponseEntity<?> cancellationAnalytics() {
        return ResponseEntity.ok(
                adminAnalyticsService.getCancellationStats()
        );
    }

    @GetMapping("/bookings/{id}/audit")
    public ResponseEntity<?> bookingAudit(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                bookingAuditRepository.findByBookingId(id)
        );
    }

}
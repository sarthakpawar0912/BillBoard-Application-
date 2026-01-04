package com.billboarding.Controller.Advertiser;

import com.billboarding.DTO.Booking.CreateBookingRequest;
import com.billboarding.DTO.Booking.PricePreviewRequest;
import com.billboarding.DTO.Booking.PricePreviewResponse;
import com.billboarding.Entity.Advertiser.FavouriteBillboard;
import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Entity.OWNER.Billboard;
import com.billboarding.Entity.User;
import com.billboarding.Repository.Advertiser.FavouriteBillboardRepository;
import com.billboarding.Repository.BillBoard.BillboardRepository;
import com.billboarding.Services.Advertiser.AdvertiserAvailabilityService;
import com.billboarding.Services.Availability.AvailabilityService;
import com.billboarding.Services.Availability.BillboardAvailabilityService;
import com.billboarding.Services.BookingService.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/advertiser")
public class AdvertiserController {

    private final BillboardRepository billboardRepository;
    private final BookingService bookingService;
    private final FavouriteBillboardRepository favRepo;
    private final BillboardAvailabilityService availabilityService;
    private final AdvertiserAvailabilityService advertiserAvailabilityService;
    private final AvailabilityService dateRangeAvailabilityService;


    public AdvertiserController(BillboardRepository billboardRepository,
                                BookingService bookingService,
                                FavouriteBillboardRepository favRepo,
                                BillboardAvailabilityService availabilityService,
                                AdvertiserAvailabilityService advertiserAvailabilityService,
                                AvailabilityService dateRangeAvailabilityService) {
        this.billboardRepository = billboardRepository;
        this.bookingService = bookingService;
        this.favRepo = favRepo;
        this.availabilityService = availabilityService;
        this.advertiserAvailabilityService = advertiserAvailabilityService;
        this.dateRangeAvailabilityService = dateRangeAvailabilityService;
    }

    // ---------------- DASHBOARD ----------------

    @GetMapping("/dashboard")
    public ResponseEntity<String> dashboard() {
        return ResponseEntity.ok("Advertiser dashboard - bookings & available billboards");
    }

    // ---------------- BILLBOARDS ----------------

    @GetMapping("/billboards")
    public ResponseEntity<List<Billboard>> getAvailableBillboards() {
        return ResponseEntity.ok(billboardRepository.findAvailableWithImagesAndOwner());
    }

    /**
     * Get billboard by ID.
     * Used for View Details functionality.
     * GET /api/advertiser/billboards/{id}
     */
    @GetMapping("/billboards/{id}")
    public ResponseEntity<Billboard> getBillboardById(@PathVariable Long id) {
        Billboard billboard = billboardRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Billboard not found with id: " + id));
        return ResponseEntity.ok(billboard);
    }

    // ---------------- BOOKINGS ----------------

    @PostMapping("/bookings")
    public ResponseEntity<Booking> createBooking(@Valid @RequestBody CreateBookingRequest req,
                                                 Authentication authentication) {

        User advertiser = (User) authentication.getPrincipal();

        // Additional validation: end date must be after or equal to start date
        if (req.getEndDate().isBefore(req.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        Booking booking = bookingService.createBooking(
                advertiser,
                req.getBillboardId(),
                req.getStartDate(),
                req.getEndDate()
        );

        return ResponseEntity.ok(booking);
    }

    // ---------------- PRICE PREVIEW (SINGLE SOURCE OF TRUTH) ----------------

    /**
     * Get price preview for a potential booking.
     * Frontend MUST use this endpoint to display prices.
     * DO NOT calculate prices on frontend - always fetch from this endpoint.
     *
     * GET /api/advertiser/bookings/price-preview?billboardId=1&startDate=2024-01-01&endDate=2024-01-05
     */
    @GetMapping("/bookings/price-preview")
    public ResponseEntity<PricePreviewResponse> getPricePreview(
            @RequestParam Long billboardId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        PricePreviewResponse preview = bookingService.calculatePricePreview(billboardId, startDate, endDate);
        return ResponseEntity.ok(preview);
    }

    /**
     * Get price preview with discount applied (for owner/negotiation flow).
     *
     * GET /api/advertiser/bookings/price-preview-discount?billboardId=1&startDate=2024-01-01&endDate=2024-01-05&discountPercent=10
     */
    @GetMapping("/bookings/price-preview-discount")
    public ResponseEntity<PricePreviewResponse> getPricePreviewWithDiscount(
            @RequestParam Long billboardId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(defaultValue = "0") Double discountPercent
    ) {
        PricePreviewResponse preview = bookingService.calculatePricePreviewWithDiscount(
                billboardId, startDate, endDate, discountPercent
        );
        return ResponseEntity.ok(preview);
    }

    @GetMapping("/billboards/{id}/availability")
    public ResponseEntity<?> getAvailability(
            @PathVariable Long id,
            @RequestParam LocalDate start,
            @RequestParam LocalDate end
    ) {
        return ResponseEntity.ok(
                availabilityService.getAvailability(id, start, end)
        );
    }

    /**
     * Check if billboard is available for date range
     * GET /api/advertiser/billboards/{id}/check-availability?startDate=2024-01-01&endDate=2024-01-05
     * Returns: { available: boolean, billboardId, startDate, endDate, message }
     */
    @GetMapping("/billboards/{id}/check-availability")
    public ResponseEntity<Map<String, Object>> checkDateRangeAvailability(
            @PathVariable Long id,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        boolean isAvailable = dateRangeAvailabilityService.isAvailable(id, startDate, endDate);

        Map<String, Object> response = new HashMap<>();
        response.put("available", isAvailable);
        response.put("billboardId", id);
        response.put("startDate", startDate.toString());
        response.put("endDate", endDate.toString());

        if (isAvailable) {
            response.put("message", "Billboard is available for the selected dates");
        } else {
            response.put("message", "Billboard is not available for the selected dates. Please choose different dates.");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<Booking>> myBookings(Authentication authentication) {
        User advertiser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(bookingService.getMyBookings(advertiser));
    }

    @PostMapping("/bookings/{id}/cancel")
    public ResponseEntity<Booking> cancelBooking(@PathVariable Long id,
                                                 Authentication authentication) {

        User advertiser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(bookingService.cancelMyBooking(advertiser, id));
    }

    @PostMapping("/bookings/{id}/cancel-after-payment")
    public ResponseEntity<Booking> cancelBookingAfterPayment(
            @PathVariable Long id,
            Authentication auth
    ) {
        User advertiser = (User) auth.getPrincipal();
        return ResponseEntity.ok(bookingService.cancelAfterPayment(advertiser, id));
    }


    // ---------------- FAVOURITES ----------------

    /**
     * Add billboard to favourites
     */
    @PostMapping("/favourites/{billboardId}")
    public ResponseEntity<String> addFavourite(@PathVariable Long billboardId,
                                               Authentication auth) {

        User advertiser = (User) auth.getPrincipal();

        if (favRepo.existsByAdvertiserAndBillboard_Id(advertiser, billboardId)) {
            return ResponseEntity.ok("Already added to favourites");
        }

        FavouriteBillboard fav = FavouriteBillboard.builder()
                .advertiser(advertiser)
                .billboard(Billboard.builder().id(billboardId).build())
                .build();

        favRepo.save(fav);
        return ResponseEntity.ok("Added to favourites");
    }

    /**
     * Get all favourites of logged-in advertiser
     */
    @GetMapping("/favourites")
    public ResponseEntity<List<FavouriteBillboard>> getMyFavourites(Authentication auth) {

        User advertiser = (User) auth.getPrincipal();
        return ResponseEntity.ok(favRepo.findByAdvertiserWithDetails(advertiser));
    }

    /**
     * Remove favourite by favourite ID
     */
    @DeleteMapping("/favourites/{favId}")
    public ResponseEntity<String> deleteFavourite(@PathVariable Long favId,
                                                  Authentication auth) {

        User advertiser = (User) auth.getPrincipal();

        FavouriteBillboard fav = favRepo.findByIdAndAdvertiser(favId, advertiser)
                .orElseThrow(() -> new RuntimeException("Favourite not found"));

        favRepo.delete(fav);
        return ResponseEntity.ok("Removed from favourites");
    }

    @GetMapping("/availability/{billboardId}")
    public ResponseEntity<?> checkAvailability(
            @PathVariable Long billboardId,
            @RequestParam LocalDate date,
            Authentication auth
    ) {
        return ResponseEntity.ok(
                advertiserAvailabilityService.getAvailability(billboardId, date)
        );
    }

}

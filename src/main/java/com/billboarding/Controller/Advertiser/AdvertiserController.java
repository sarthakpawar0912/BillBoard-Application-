package com.billboarding.Controller.Advertiser;

import com.billboarding.DTO.Booking.CreateBookingRequest;
import com.billboarding.Entity.Advertiser.FavouriteBillboard;
import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Entity.OWNER.Billboard;
import com.billboarding.Entity.User;
import com.billboarding.Repository.Advertiser.FavouriteBillboardRepository;
import com.billboarding.Repository.BillBoard.BillboardRepository;
import com.billboarding.Services.Advertiser.AdvertiserAvailabilityService;
import com.billboarding.Services.Availability.BillboardAvailabilityService;
import com.billboarding.Services.BookingService.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/advertiser")
public class AdvertiserController {

    private final BillboardRepository billboardRepository;
    private final BookingService bookingService;
    private final FavouriteBillboardRepository favRepo;
    private final BillboardAvailabilityService availabilityService;
    private final AdvertiserAvailabilityService advertiserAvailabilityService;


    public AdvertiserController(BillboardRepository billboardRepository,
                                BookingService bookingService,
                                FavouriteBillboardRepository favRepo, BillboardAvailabilityService availabilityService, AdvertiserAvailabilityService advertiserAvailabilityService) {
        this.billboardRepository = billboardRepository;
        this.bookingService = bookingService;
        this.favRepo = favRepo;
        this.availabilityService = availabilityService;
        this.advertiserAvailabilityService = advertiserAvailabilityService;
    }

    // ---------------- DASHBOARD ----------------

    @GetMapping("/dashboard")
    public ResponseEntity<String> dashboard() {
        return ResponseEntity.ok("Advertiser dashboard - bookings & available billboards");
    }

    // ---------------- BILLBOARDS ----------------

    @GetMapping("/billboards")
    public ResponseEntity<List<Billboard>> getAvailableBillboards() {
        return ResponseEntity.ok(billboardRepository.findByAvailableTrue());
    }

    // ---------------- BOOKINGS ----------------

    @PostMapping("/bookings")
    public ResponseEntity<Booking> createBooking(@RequestBody CreateBookingRequest req,
                                                 Authentication authentication) {

        User advertiser = (User) authentication.getPrincipal();

        Booking booking = bookingService.createBooking(
                advertiser,
                req.getBillboardId(),
                req.getStartDate(),
                req.getEndDate()
        );

        return ResponseEntity.ok(booking);
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

    @GetMapping("/bookings")
    public ResponseEntity<List<Booking>> myBookings(Authentication authentication) {
        User advertiser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(bookingService.getMyBookings(advertiser));
    }

    @PostMapping("/bookings/{id}/cancel")
    public ResponseEntity<Booking> cancelBooking(@PathVariable Long id,
                                                 Authentication authentication) {

        User advertiser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(
                bookingService.cancelMyBooking(advertiser, id)
        );
    }
    @PostMapping("/bookings/{id}/cancel-after-payment")
    public ResponseEntity<?> cancelBookingAfterPayment(
            @PathVariable Long id,
            Authentication auth
    ) {
        User advertiser = (User) auth.getPrincipal();
        return ResponseEntity.ok(
                bookingService.cancelAfterPayment(advertiser, id)
        );
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
        return ResponseEntity.ok(favRepo.findByAdvertiser(advertiser));
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

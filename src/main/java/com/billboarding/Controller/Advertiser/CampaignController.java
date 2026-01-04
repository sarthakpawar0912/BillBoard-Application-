package com.billboarding.Controller.Advertiser;

import com.billboarding.DTO.Advertiser.CampaignCreateDTO;
import com.billboarding.DTO.Advertiser.CampaignDailyAnalyticsDTO;
import com.billboarding.Entity.Advertiser.Campaign;
import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Entity.User;
import com.billboarding.Repository.Advertiser.CampaignRepository;
import com.billboarding.Repository.Booking.BookingRepository;
import com.billboarding.Services.Advertiser.CampaignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/advertiser/campaigns")
@RequiredArgsConstructor
@CrossOrigin(
        origins = {
                "http://localhost:4200",
                "http://localhost:4201",
                "http://localhost:4202"
        },
        allowedHeaders = {
                "Authorization",
                "Content-Type",
                "X-Requested-With"
        },
        methods = {
                RequestMethod.GET,
                RequestMethod.POST,
                RequestMethod.PUT,
                RequestMethod.PATCH,
                RequestMethod.DELETE,
                RequestMethod.OPTIONS
        },
        allowCredentials = "true"
)
public class CampaignController {

    private final CampaignService campaignService;
    private final CampaignRepository campaignRepo;
    private final BookingRepository bookingRepo;

    // 1️⃣ CREATE CAMPAIGN
    @PostMapping
    public Campaign create(
            @Valid @RequestBody CampaignCreateDTO dto,
            Authentication auth
    ) {
        // Additional validation: end date must be after or equal to start date
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        return campaignService.createCampaign(
                dto,
                (User) auth.getPrincipal()
        );
    }

    // 2️⃣ LIST MY CAMPAIGNS
    @GetMapping
    public List<Campaign> list(Authentication auth) {
        return campaignService.myCampaigns(
                (User) auth.getPrincipal()
        );
    }

    // 3️⃣ ATTACH BOOKING TO CAMPAIGN
    @PostMapping("/{campaignId}/attach-booking/{bookingId}")
    public ResponseEntity<?> attachBooking(
            @PathVariable Long campaignId,
            @PathVariable Long bookingId,
            Authentication auth
    ) {
        User advertiser = (User) auth.getPrincipal();

        Campaign campaign = campaignRepo.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        if (!campaign.getAdvertiser().getId().equals(advertiser.getId())) {
            return ResponseEntity.status(403).body("Unauthorized");
        }

        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getAdvertiser().getId().equals(advertiser.getId())) {
            return ResponseEntity.status(403)
                    .body("This booking does not belong to you");
        }

        campaignService.attachBooking(campaign, booking);
        return ResponseEntity.ok("Booking attached successfully");
    }

    // 4️⃣ DETACH BOOKING FROM CAMPAIGN
    @DeleteMapping("/{campaignId}/detach-booking/{bookingId}")
    public ResponseEntity<?> detachBooking(
            @PathVariable Long campaignId,
            @PathVariable Long bookingId,
            Authentication auth
    ) {
        User advertiser = (User) auth.getPrincipal();

        Campaign campaign = campaignRepo.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        if (!campaign.getAdvertiser().getId().equals(advertiser.getId())) {
            return ResponseEntity.status(403).body("Unauthorized");
        }

        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        campaignService.detachBooking(campaign, booking);
        return ResponseEntity.ok("Booking detached successfully");
    }

    // 5️⃣ DAILY ANALYTICS
    @GetMapping("/{campaignId}/daily-analytics")
    public List<CampaignDailyAnalyticsDTO> dailyAnalytics(
            @PathVariable Long campaignId,
            Authentication auth
    ) {
        return campaignService.dailyAnalytics(campaignId);
    }

    // 6️⃣ PAUSE CAMPAIGN
    @PatchMapping("/{campaignId}/pause")
    public Campaign pause(
            @PathVariable Long campaignId,
            Authentication auth
    ) {
        return campaignService.pause(
                campaignId,
                (User) auth.getPrincipal()
        );
    }

    // 7️⃣ RESUME CAMPAIGN
    @PatchMapping("/{campaignId}/resume")
    public Campaign resume(
            @PathVariable Long campaignId,
            Authentication auth
    ) {
        return campaignService.resume(
                campaignId,
                (User) auth.getPrincipal()
        );
    }

    // 8️⃣ DELETE CAMPAIGN
    @DeleteMapping("/{campaignId}")
    public ResponseEntity<?> deleteCampaign(
            @PathVariable Long campaignId,
            Authentication auth
    ) {
        campaignService.delete(
                campaignId,
                (User) auth.getPrincipal()
        );
        return ResponseEntity.ok("Campaign deleted successfully");
    }
}

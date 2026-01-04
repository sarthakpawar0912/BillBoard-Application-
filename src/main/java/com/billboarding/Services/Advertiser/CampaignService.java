package com.billboarding.Services.Advertiser;

import com.billboarding.DTO.Advertiser.CampaignCreateDTO;
import com.billboarding.DTO.Advertiser.CampaignDailyAnalyticsDTO;
import com.billboarding.ENUM.CampaignStatus;
import com.billboarding.ENUM.PaymentStatus;
import com.billboarding.Entity.Advertiser.Campaign;
import com.billboarding.Entity.Advertiser.CampaignBooking;
import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Entity.User;
import com.billboarding.Repository.Advertiser.CampaignBookingRepository;
import com.billboarding.Repository.Advertiser.CampaignRepository;
import com.billboarding.Repository.Booking.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepo;
    private final CampaignBookingRepository cbRepo;
    private final BookingRepository bookingRepo;

    /* =========================================================
       1️⃣ CREATE CAMPAIGN
       ========================================================= */
    public Campaign createCampaign(CampaignCreateDTO dto, User advertiser) {
        return campaignRepo.save(
                Campaign.builder()
                        .name(dto.getName())
                        .budget(dto.getBudget())
                        .startDate(dto.getStartDate())
                        .endDate(dto.getEndDate())
                        .cities(dto.getCities())
                        .advertiser(advertiser)
                        .status(CampaignStatus.SCHEDULED)
                        .spent(0.0)
                        .build()
        );
    }

    /* =========================================================
       2️⃣ LIST MY CAMPAIGNS
       ========================================================= */
    public List<Campaign> myCampaigns(User advertiser) {
        return campaignRepo.findByAdvertiser(advertiser);
    }

    /* =========================================================
       3️⃣ ATTACH BOOKING TO CAMPAIGN
       ========================================================= */
    public void attachBooking(Campaign campaign, Booking booking) {

        // ✅ Booking must be PAID
        if (booking.getPaymentStatus() != PaymentStatus.PAID) {
            throw new RuntimeException("Only PAID bookings can be attached");
        }

        // ✅ Booking must not already belong to a campaign
        if (booking.getCampaign() != null) {
            throw new RuntimeException(
                    "Booking already attached to campaign #" +
                            booking.getCampaign().getId()
            );
        }

        // ✅ Campaign must be ACTIVE / SCHEDULED
        if (campaign.getStatus() == CampaignStatus.CANCELLED ||
                campaign.getStatus() == CampaignStatus.COMPLETED) {
            throw new RuntimeException(
                    "Cannot attach booking to " + campaign.getStatus() + " campaign"
            );
        }

        // ✅ Budget check
        double newSpent = campaign.getSpent() + booking.getTotalPrice();
        if (newSpent > campaign.getBudget()) {
            throw new RuntimeException(
                    String.format(
                            "Budget exceeded: %.2f + %.2f > %.2f",
                            campaign.getSpent(),
                            booking.getTotalPrice(),
                            campaign.getBudget()
                    )
            );
        }

        // ✅ Save association
        cbRepo.save(
                CampaignBooking.builder()
                        .campaign(campaign)
                        .booking(booking)
                        .build()
        );

        // ✅ Update campaign spend
        campaign.setSpent(newSpent);
        campaignRepo.save(campaign);

        // ✅ Update booking reference
        booking.setCampaign(campaign);
        bookingRepo.save(booking);
    }

    /* =========================================================
       4️⃣ DETACH BOOKING FROM CAMPAIGN
       ========================================================= */
    public void detachBooking(Campaign campaign, Booking booking) {

        CampaignBooking cb = cbRepo
                .findByCampaignAndBooking(campaign, booking)
                .orElseThrow(() ->
                        new RuntimeException("Booking not attached to this campaign")
                );

        // Remove association
        cbRepo.delete(cb);

        // Update campaign spent
        campaign.setSpent(
                campaign.getSpent() - booking.getTotalPrice()
        );
        campaignRepo.save(campaign);

        // Clear booking campaign reference
        booking.setCampaign(null);
        bookingRepo.save(booking);
    }

    /* =========================================================
       5️⃣ DAILY ANALYTICS
       ========================================================= */
    public List<CampaignDailyAnalyticsDTO> dailyAnalytics(Long campaignId) {

        Campaign campaign = campaignRepo.findById(campaignId)
                .orElseThrow(() ->
                        new RuntimeException("Campaign not found")
                );

        Map<LocalDate, Double> dailySpend =
                cbRepo.findByCampaign(campaign)
                        .stream()
                        .collect(Collectors.groupingBy(
                                cb -> cb.getBooking().getStartDate(),
                                Collectors.summingDouble(
                                        cb -> cb.getBooking().getTotalPrice()
                                )
                        ));

        return dailySpend.entrySet()
                .stream()
                .map(e -> new CampaignDailyAnalyticsDTO(
                        e.getKey(),
                        e.getValue(),
                        (long) (e.getValue() * 20) // impression rule
                ))
                .toList();
    }

    /* =========================================================
       6️⃣ PAUSE CAMPAIGN
       ========================================================= */
    public Campaign pause(Long campaignId, User advertiser) {

        Campaign campaign = campaignRepo.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        validateOwner(campaign, advertiser);

        campaign.setStatus(CampaignStatus.PAUSED);
        return campaignRepo.save(campaign);
    }

    /* =========================================================
       7️⃣ RESUME CAMPAIGN
       ========================================================= */
    public Campaign resume(Long campaignId, User advertiser) {

        Campaign campaign = campaignRepo.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        validateOwner(campaign, advertiser);

        campaign.setStatus(CampaignStatus.ACTIVE);
        return campaignRepo.save(campaign);
    }

    /* =========================================================
       8️⃣ DELETE CAMPAIGN (FK SAFE)
       ========================================================= */
    public void delete(Long campaignId, User advertiser) {

        Campaign campaign = campaignRepo.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        validateOwner(campaign, advertiser);

        // 1️⃣ Remove campaign-booking links
        cbRepo.deleteByCampaignId(campaignId);

        // 2️⃣ Clear campaign reference from bookings
        bookingRepo.clearCampaign(campaignId);

        // 3️⃣ Delete campaign
        campaignRepo.delete(campaign);
    }

    /* =========================================================
       🔒 COMMON OWNER VALIDATION
       ========================================================= */
    private void validateOwner(Campaign campaign, User advertiser) {
        if (!campaign.getAdvertiser().getId().equals(advertiser.getId())) {
            throw new RuntimeException("Unauthorized");
        }
    }
}

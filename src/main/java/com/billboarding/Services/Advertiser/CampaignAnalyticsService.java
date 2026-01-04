package com.billboarding.Services.Advertiser;

import com.billboarding.DTO.Advertiser.CampaignAnalyticsDTO;
import com.billboarding.Entity.Advertiser.Campaign;
import com.billboarding.Entity.User;
import com.billboarding.Repository.Advertiser.CampaignBookingRepository;
import com.billboarding.Repository.Advertiser.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CampaignAnalyticsService {

    private final CampaignRepository campaignRepo;
    private final CampaignBookingRepository cbRepo;

    public CampaignAnalyticsDTO analytics(Long campaignId, User advertiser) {

        Campaign campaign = campaignRepo.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        if (!campaign.getAdvertiser().getId().equals(advertiser.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        BigDecimal spent = BigDecimal.valueOf(
                cbRepo.findByCampaign(campaign)
                        .stream()
                        .mapToDouble(cb -> cb.getBooking().getTotalPrice())
                        .sum()
        );

        long impressions = spent.longValue() * 20; // business rule
        double cpm = impressions == 0 ? 0 : (spent.doubleValue() / impressions) * 1000;

        return CampaignAnalyticsDTO.builder()
                .campaignId(campaign.getId())
                .campaignName(campaign.getName())
                .status(campaign.getStatus().name())
                .budget(campaign.getBudget())
                .spent(spent)
                .impressions(impressions)
                .cpm(cpm)
                .budgetUtilization(
                        spent.doubleValue() / campaign.getBudget() * 100
                )
                .startDate(campaign.getStartDate())
                .endDate(campaign.getEndDate())
                .build();
    }
}

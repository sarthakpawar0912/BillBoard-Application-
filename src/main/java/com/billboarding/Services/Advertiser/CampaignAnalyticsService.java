package com.billboarding.Services.Advertiser;
import com.billboarding.DTO.Advertiser.CampaignAnalyticsDTO;
import com.billboarding.Entity.Advertiser.Campaign;
import com.billboarding.Entity.User;
import com.billboarding.Repository.Advertiser.CampaignBookingRepository;
import com.billboarding.Repository.Advertiser.CampaignRepository;
import com.billboarding.Repository.Payment.PaymentHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;


@Service
@RequiredArgsConstructor
public class CampaignAnalyticsService {

    private final CampaignRepository campaignRepo;
    private final CampaignBookingRepository cbRepo;
    private final PaymentHistoryRepository paymentRepo;

    public CampaignAnalyticsDTO getAnalytics(Long campaignId, User advertiser) {

        Campaign campaign = campaignRepo.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        if (!campaign.getAdvertiser().getId().equals(advertiser.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        // Total spend from payments
        BigDecimal spent = paymentRepo.findAll().stream()
                .filter(p -> cbRepo.findByCampaign(campaign).stream()
                        .anyMatch(cb -> cb.getBooking().getId()
                                .equals(p.getBooking().getId())))
                .map(p -> BigDecimal.valueOf(p.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Simulated impressions (REALISTIC placeholder)
        long impressions = spent.longValue() * 20; // 1₹ ≈ 20 impressions

        double cpm = impressions == 0 ? 0 :
                (spent.doubleValue() / impressions) * 1000;

        double utilization = spent.doubleValue() / campaign.getBudget().doubleValue() * 100;

        return CampaignAnalyticsDTO.builder()
                .campaignId(campaign.getId())
                .campaignName(campaign.getName())
                .status(campaign.getStatus().name())
                .budget(campaign.getBudget())
                .spent(spent)
                .impressions(impressions)
                .cpm(cpm)
                .budgetUtilization(utilization)
                .startDate(campaign.getStartDate())
                .endDate(campaign.getEndDate())
                .build();
    }
}

package com.billboarding.DTO.Advertiser;

import java.time.LocalDate;

public record CampaignDailyAnalyticsDTO(
        LocalDate date,
        Double spent,
        Long impressions
) {}

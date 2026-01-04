package com.billboarding.DTO.Advertiser;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class CampaignAnalyticsDTO {

    private Long campaignId;
    private String campaignName;
    private String status;

    private Double budget;
    private BigDecimal spent;

    private Long impressions;
    private Double cpm;
    private Double budgetUtilization;

    private LocalDate startDate;
    private LocalDate endDate;
}

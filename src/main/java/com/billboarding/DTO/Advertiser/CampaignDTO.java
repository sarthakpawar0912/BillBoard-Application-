package com.billboarding.DTO.Advertiser;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CampaignDTO {
    private String name;
    private Integer billboards;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal budget;
    private List<String> cities;
}

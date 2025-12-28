package com.billboarding.DTO.OWNER;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class OwnerAnalyticsResponse {

    private double totalRevenue;
    private List<BillboardRevenue> billboardRevenues;
    private BillboardRevenue topPerformingBillboard;
    private Map<String, Double> monthlyRevenues;

    @Data
    public static class BillboardRevenue {
        private Long billboardId;
        private String title;
        private int totalBookings;
        private double revenue;
        private int imageCount;
    }
}
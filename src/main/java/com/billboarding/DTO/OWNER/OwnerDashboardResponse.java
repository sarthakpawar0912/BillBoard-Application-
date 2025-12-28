package com.billboarding.DTO.OWNER;


import lombok.Data;

import java.util.List;

@Data
public class OwnerDashboardResponse {

    private double totalEarnings;
    private long totalBillboards;
    private long totalBookings;
    private long totalPendingRequests;

    private List<BillboardAnalytics> billboardStats;

    @Data
    public static class BillboardAnalytics {
        private Long billboardId;
        private String title;
        private long totalBookings;
        private double totalRevenue;
        private int imageCount;
    }
}
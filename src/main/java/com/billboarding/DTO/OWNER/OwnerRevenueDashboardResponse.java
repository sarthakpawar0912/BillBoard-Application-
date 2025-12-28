package com.billboarding.DTO.OWNER;

import lombok.Data;

import java.util.List;

@Data
public class OwnerRevenueDashboardResponse {

    // HIGH-LEVEL SUMMARY
    private double totalEarnings;       // Sum of APPROVED bookings revenue
    private long totalBillboards;       // Number of billboards owned
    private long totalBookings;         // All bookings (any status)
    private long pendingRequests;       // PENDING bookings

    // PER-BILLBOARD ANALYTICS
    private List<BillboardRevenueDTO> billboards;

    // MONTHLY REVENUE (for chart)
    private List<MonthlyRevenueDTO> monthlyRevenue;

    // HEATMAP POINTS (for map visualizations)
    private List<HeatmapPointDTO> heatmapPoints;
}
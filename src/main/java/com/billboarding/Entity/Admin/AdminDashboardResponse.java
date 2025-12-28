package com.billboarding.Entity.Admin;
import lombok.Data;

@Data
public class AdminDashboardResponse {

    // USER STATS
    private long totalUsers;
    private long totalOwners;
    private long totalAdvertisers;
    private long totalPendingKyc;
    private long totalBlockedUsers;

    // BILLBOARD STATS
    private long totalBillboards;
    private long availableBillboards;
    private long bookedBillboards;

    // BOOKING STATS
    private long totalBookings;
    private long pendingBookings;
    private long approvedBookings;
    private long rejectedBookings;
    private long cancelledBookings;

    // REVENUE
    private double totalRevenue;
}

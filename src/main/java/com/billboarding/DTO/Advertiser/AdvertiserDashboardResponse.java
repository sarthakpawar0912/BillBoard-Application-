package com.billboarding.DTO.Advertiser;

import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Entity.OWNER.Billboard;
import lombok.Data;

import java.util.List;

@Data
public class AdvertiserDashboardResponse {

    private long totalBookings;
    private long pending;
    private long approved;
    private long rejected;
    private long cancelled;

    private List<Booking> recentBookings;
    private List<Billboard> favouriteBillboards;
}

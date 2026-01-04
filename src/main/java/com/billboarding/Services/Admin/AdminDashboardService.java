package com.billboarding.Services.Admin;

import com.billboarding.ENUM.BookingStatus;
import com.billboarding.ENUM.KycStatus;
import com.billboarding.ENUM.UserRole;
import com.billboarding.Entity.Admin.AdminDashboardResponse;
import com.billboarding.Repository.BillBoard.BillboardRepository;
import com.billboarding.Repository.Booking.BookingRepository;
import com.billboarding.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final BillboardRepository billboardRepository;
    private final BookingRepository bookingRepository;

    public AdminDashboardResponse getDashboardData() {

        AdminDashboardResponse res = new AdminDashboardResponse();

        // USER STATS - Using optimized COUNT queries (no entity loading)
        res.setTotalUsers(userRepository.count());
        res.setTotalOwners(userRepository.countByRole(UserRole.OWNER));
        res.setTotalAdvertisers(userRepository.countByRole(UserRole.ADVERTISER));
        res.setTotalPendingKyc(userRepository.countByKycStatus(KycStatus.PENDING));
        res.setTotalBlockedUsers(userRepository.countByBlockedTrue());

        // BILLBOARD STATS - Using optimized COUNT queries
        res.setTotalBillboards(billboardRepository.count());
        res.setAvailableBillboards(billboardRepository.countByAvailableTrue());
        res.setBookedBillboards(bookingRepository.countByStatus(BookingStatus.APPROVED));

        // BOOKING STATS - Using optimized COUNT queries
        res.setTotalBookings(bookingRepository.count());
        res.setPendingBookings(bookingRepository.countByStatus(BookingStatus.PENDING));
        res.setApprovedBookings(bookingRepository.countByStatus(BookingStatus.APPROVED));
        res.setRejectedBookings(bookingRepository.countByStatus(BookingStatus.REJECTED));
        res.setCancelledBookings(bookingRepository.countByStatus(BookingStatus.CANCELLED));

        // REVENUE - Using optimized SUM query (no entity loading!)
        res.setTotalRevenue(bookingRepository.sumTotalPriceByStatus(BookingStatus.APPROVED));

        return res;
    }
}

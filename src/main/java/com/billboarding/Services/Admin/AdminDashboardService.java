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

        // USER STATS
        res.setTotalUsers(userRepository.count());
        res.setTotalOwners(userRepository.findByRole(UserRole.OWNER).size());
        res.setTotalAdvertisers(userRepository.findByRole(UserRole.ADVERTISER).size());
        res.setTotalPendingKyc(userRepository.findByKycStatus(KycStatus.PENDING).size());
        res.setTotalBlockedUsers(userRepository.findByBlockedTrue().size());

        // BILLBOARD STATS
        res.setTotalBillboards(billboardRepository.count());
        res.setAvailableBillboards(billboardRepository.findByAvailableTrue().size());
        res.setBookedBillboards(bookingRepository.findByStatus(BookingStatus.APPROVED).size());

        // BOOKING STATS
        res.setTotalBookings(bookingRepository.count());
        res.setPendingBookings(bookingRepository.findByStatus(BookingStatus.PENDING).size());
        res.setApprovedBookings(bookingRepository.findByStatus(BookingStatus.APPROVED).size());
        res.setRejectedBookings(bookingRepository.findByStatus(BookingStatus.REJECTED).size());
        res.setCancelledBookings(bookingRepository.findByStatus(BookingStatus.CANCELLED).size());

        // REVENUE (approved bookings only for now)
        res.setTotalRevenue(
                bookingRepository.findByStatus(BookingStatus.APPROVED)
                        .stream()
                        .mapToDouble(b -> b.getTotalPrice())
                        .sum()
        );
        return res;
    }
}

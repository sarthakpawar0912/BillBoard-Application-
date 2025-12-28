package com.billboarding.Services.Owner;

import com.billboarding.DTO.OWNER.OwnerDashboardResponse;
import com.billboarding.Entity.OWNER.Billboard;
import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.ENUM.BookingStatus;
import com.billboarding.Entity.User;
import com.billboarding.Repository.BillBoard.BillboardRepository;
import com.billboarding.Repository.Booking.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerDashboardService {

    private final BillboardRepository billboardRepository;
    private final BookingRepository bookingRepository;

    public OwnerDashboardResponse getDashboard(User owner) {

        List<Billboard> myBoards = billboardRepository.findByOwner(owner);

        List<Booking> myBookings =
                bookingRepository.findByBillboard_Owner(owner);

        OwnerDashboardResponse res = new OwnerDashboardResponse();

        // earnings (only APPROVED + PAID)
        double totalEarnings = myBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.APPROVED)
                .mapToDouble(Booking::getTotalPrice)
                .sum();

        res.setTotalEarnings(totalEarnings);
        res.setTotalBillboards(myBoards.size());
        res.setTotalBookings(myBookings.size());
        res.setTotalPendingRequests(
                myBookings.stream().filter(b -> b.getStatus() == BookingStatus.PENDING).count()
        );

        // Billboard analytics
        res.setBillboardStats(
                myBoards.stream().map(b -> {
                    OwnerDashboardResponse.BillboardAnalytics a =
                            new OwnerDashboardResponse.BillboardAnalytics();
                    a.setBillboardId(b.getId());
                    a.setTitle(b.getTitle());
                    a.setImageCount(b.getImagePaths().size());

                    List<Booking> bb =
                            bookingRepository.findByBillboard_Id(b.getId());

                    a.setTotalBookings(bb.size());

                    a.setTotalRevenue(
                            bb.stream()
                                    .filter(x -> x.getStatus() == BookingStatus.APPROVED)
                                    .mapToDouble(Booking::getTotalPrice)
                                    .sum()
                    );

                    return a;
                }).collect(Collectors.toList())
        );

        return res;
    }
}
package com.billboarding.Services.Owner;

import com.billboarding.DTO.OWNER.OwnerAnalyticsResponse;
import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Entity.OWNER.Billboard;
import com.billboarding.Entity.User;
import com.billboarding.ENUM.BookingStatus;
import com.billboarding.Repository.BillBoard.BillboardRepository;
import com.billboarding.Repository.Booking.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerAnalyticsService {

    private final BillboardRepository billboardRepo;
    private final BookingRepository bookingRepo;

    public OwnerAnalyticsResponse getAnalytics(User owner) {

        List<Billboard> boards = billboardRepo.findByOwner(owner);
        List<Booking> bookings = bookingRepo.findByBillboard_Owner(owner);

        OwnerAnalyticsResponse res = new OwnerAnalyticsResponse();

        res.setTotalRevenue(
                bookings.stream()
                        .filter(b -> b.getStatus() == BookingStatus.APPROVED)
                        .mapToDouble(Booking::getTotalPrice)
                        .sum()
        );

        res.setBillboardRevenues(
                boards.stream().map(b -> {
                    OwnerAnalyticsResponse.BillboardRevenue r =
                            new OwnerAnalyticsResponse.BillboardRevenue();

                    r.setBillboardId(b.getId());
                    r.setTitle(b.getTitle());

                    List<Booking> bb = bookingRepo.findByBillboard_Id(b.getId());

                    r.setTotalBookings(bb.size());
                    r.setRevenue(
                            bb.stream()
                                    .filter(x -> x.getStatus() == BookingStatus.APPROVED)
                                    .mapToDouble(Booking::getTotalPrice)
                                    .sum()
                    );

                    r.setImageCount(
                            b.getImagePaths() != null ? b.getImagePaths().size() : 0
                    );

                    return r;
                }).toList()
        );

        res.setTopPerformingBillboard(
                res.getBillboardRevenues().stream()
                        .max(Comparator.comparingDouble(OwnerAnalyticsResponse.BillboardRevenue::getRevenue))
                        .orElse(null)
        );

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");

        res.setMonthlyRevenues(
                bookings.stream()
                        .filter(b -> b.getStatus() == BookingStatus.APPROVED)
                        .collect(Collectors.groupingBy(
                                b -> b.getCreatedAt().format(fmt),
                                Collectors.summingDouble(Booking::getTotalPrice)
                        ))
        );

        return res;
    }
}

package com.billboarding.Services.Owner;

import com.billboarding.DTO.OWNER.*;
import com.billboarding.ENUM.BookingStatus;
import com.billboarding.ENUM.PaymentStatus;
import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Entity.OWNER.Billboard;
import com.billboarding.Entity.User;
import com.billboarding.Repository.BillBoard.BillboardRepository;
import com.billboarding.Repository.Booking.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class OwnerRevenueService {

    private final BillboardRepository billboardRepository;
    private final BookingRepository bookingRepository;

    /**
     * MAIN METHOD WITH FILTERS
     */
    public OwnerRevenueDashboardResponse getRevenueDashboard(
            User owner,
            Long billboardId,
            LocalDate start,
            LocalDate end
    ) {

        // 1️⃣ Base billboards belonging to this owner
        List<Billboard> boards = billboardRepository.findByOwner(owner);

        // If filtering by billboard
        if (billboardId != null) {
            boards = boards.stream()
                    .filter(b -> b.getId().equals(billboardId))
                    .collect(Collectors.toList());
        }

        // 2️⃣ Get bookings for owner
        List<Booking> allBookings = bookingRepository.findByBillboard_Owner(owner);

        // Apply billboard filter
        if (billboardId != null) {
            allBookings = allBookings.stream()
                    .filter(b -> b.getBillboard().getId().equals(billboardId))
                    .collect(Collectors.toList());
        }

        // Apply date range filter
        if (start != null && end != null) {
            allBookings = allBookings.stream()
                    .filter(b ->
                            !(b.getEndDate().isBefore(start) || b.getStartDate().isAfter(end))
                    )
                    .collect(Collectors.toList());
        }

        OwnerRevenueDashboardResponse res = new OwnerRevenueDashboardResponse();

        // ---------- SUMMARY ----------
        double totalEarnings = allBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.APPROVED)
                .mapToDouble(Booking::getTotalPrice)
                .sum();

        long pendingRequests = allBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.PENDING)
                .count();

        res.setTotalEarnings(totalEarnings);
        res.setTotalBillboards(boards.size());
        res.setTotalBookings(allBookings.size());
        res.setPendingRequests(pendingRequests);

        // ---------- PER BILLBOARD ----------
        List<Booking> finalAllBookings = allBookings;
        List<BillboardRevenueDTO> stats = boards.stream()
                .map(board -> {

                    List<Booking> boardBookings = finalAllBookings.stream()
                            .filter(b -> b.getBillboard().getId().equals(board.getId()))
                            .collect(Collectors.toList());

                    BillboardRevenueDTO dto = new BillboardRevenueDTO();

                    dto.setBillboardId(board.getId());
                    dto.setTitle(board.getTitle());
                    dto.setLocation(board.getLocation());
                    dto.setType(board.getType() != null ? board.getType().name() : null);
                    dto.setImageCount(board.getImagePaths().size());
                    dto.setLatitude(board.getLatitude());
                    dto.setLongitude(board.getLongitude());

                    dto.setTotalBookings(boardBookings.size());

                    double revenue = boardBookings.stream()
                            .filter(b -> b.getStatus() == BookingStatus.APPROVED)
                            .mapToDouble(Booking::getTotalPrice)
                            .sum();
                    dto.setTotalRevenue(revenue);

                    return dto;
                })
                .collect(Collectors.toList());

        res.setBillboards(stats);

        // ---------- MONTHLY CHART ----------
        Map<YearMonth, Double> monthMap = new HashMap<>();

        allBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.APPROVED)
                .forEach(b -> {
                    YearMonth ym = YearMonth.from(b.getStartDate());
                    monthMap.merge(ym, b.getTotalPrice(), Double::sum);
                });

        List<MonthlyRevenueDTO> monthlyRevenue = monthMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    MonthlyRevenueDTO m = new MonthlyRevenueDTO();
                    m.setYear(e.getKey().getYear());
                    m.setMonth(e.getKey().getMonthValue());
                    m.setTotalRevenue(e.getValue());
                    return m;
                })
                .collect(Collectors.toList());

        res.setMonthlyRevenue(monthlyRevenue);

        // ---------- HEATMAP ----------
        List<HeatmapPointDTO> heat = stats.stream()
                .map(stat -> {

                    HeatmapPointDTO h = new HeatmapPointDTO();
                    h.setLatitude(stat.getLatitude());
                    h.setLongitude(stat.getLongitude());
                    h.setIntensity(stat.getTotalRevenue());
                    return h;
                })
                .collect(Collectors.toList());

        res.setHeatmapPoints(heat);

        return res;
    }


    public List<OwnerMonthlyRevenueDTO> getMonthlyRevenue(User owner) {

        List<Booking> bookings =
                bookingRepository.findByBillboard_OwnerAndStatusAndPaymentStatus(
                        owner,
                        BookingStatus.APPROVED,
                        PaymentStatus.PAID
                );

        return bookings.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getStartDate().getYear() + "-" +
                                String.format("%02d", b.getStartDate().getMonthValue()),
                        Collectors.summingDouble(Booking::getTotalPrice)
                ))
                .entrySet().stream()
                .map(e -> new OwnerMonthlyRevenueDTO(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(OwnerMonthlyRevenueDTO::getMonth))
                .toList();
    }
}

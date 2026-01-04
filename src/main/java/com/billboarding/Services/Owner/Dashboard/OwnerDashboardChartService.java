package com.billboarding.Services.Owner.Dashboard;

import com.billboarding.DTO.ChartPointDTO;
import com.billboarding.ENUM.PaymentStatus;
import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Entity.User;
import com.billboarding.Repository.Booking.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerDashboardChartService {

    private final BookingRepository bookingRepo;

    /**
     * Monthly revenue for owner (base amount - what owner earns)
     */
    public List<ChartPointDTO> monthlyRevenue(User owner) {
        List<Booking> paidBookings = bookingRepo.findByBillboard_Owner(owner)
                .stream()
                .filter(b -> b.getPaymentStatus() == PaymentStatus.PAID && b.getPaymentDate() != null)
                .toList();

        Map<Month, Double> monthlyMap = new LinkedHashMap<>();

        // Initialize all months with 0
        for (Month month : Month.values()) {
            monthlyMap.put(month, 0.0);
        }

        // Sum up revenue by month
        for (Booking b : paidBookings) {
            Month m = b.getPaymentDate().getMonth();
            monthlyMap.put(m, monthlyMap.get(m) + (b.getBaseAmount() != null ? b.getBaseAmount() : 0.0));
        }

        return monthlyMap.entrySet().stream()
                .map(e -> new ChartPointDTO(e.getKey().name(), e.getValue()))
                .toList();
    }

    /**
     * Monthly bookings count for owner
     */
    public List<ChartPointDTO> monthlyBookingsCount(User owner) {
        List<Booking> paidBookings = bookingRepo.findByBillboard_Owner(owner)
                .stream()
                .filter(b -> b.getPaymentStatus() == PaymentStatus.PAID && b.getPaymentDate() != null)
                .toList();

        Map<Month, Long> monthlyMap = new LinkedHashMap<>();

        for (Month month : Month.values()) {
            monthlyMap.put(month, 0L);
        }

        for (Booking b : paidBookings) {
            Month m = b.getPaymentDate().getMonth();
            monthlyMap.put(m, monthlyMap.get(m) + 1);
        }

        return monthlyMap.entrySet().stream()
                .map(e -> new ChartPointDTO(e.getKey().name(), e.getValue().doubleValue()))
                .toList();
    }

    /**
     * Revenue by billboard for owner
     */
    public List<ChartPointDTO> revenueByBillboard(User owner) {
        List<Booking> paidBookings = bookingRepo.findByBillboard_Owner(owner)
                .stream()
                .filter(b -> b.getPaymentStatus() == PaymentStatus.PAID)
                .toList();

        Map<String, Double> billboardMap = paidBookings.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getBillboard().getTitle(),
                        Collectors.summingDouble(b -> b.getBaseAmount() != null ? b.getBaseAmount() : 0.0)
                ));

        return billboardMap.entrySet().stream()
                .map(e -> new ChartPointDTO(e.getKey(), e.getValue()))
                .toList();
    }
}

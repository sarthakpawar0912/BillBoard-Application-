package com.billboarding.Services.Admin.Dashboard;

import com.billboarding.DTO.ChartPointDTO;
import com.billboarding.ENUM.PaymentStatus;
import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Repository.Booking.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardChartService {

    private final BookingRepository bookingRepo;

    /**
     * Monthly platform commission revenue
     */
    public List<ChartPointDTO> monthlyCommissionRevenue() {
        List<Booking> paidBookings = bookingRepo.findAll()
                .stream()
                .filter(b -> b.getPaymentStatus() == PaymentStatus.PAID && b.getPaymentDate() != null)
                .toList();

        Map<Month, Double> monthlyMap = new LinkedHashMap<>();

        for (Month month : Month.values()) {
            monthlyMap.put(month, 0.0);
        }

        for (Booking b : paidBookings) {
            Month m = b.getPaymentDate().getMonth();
            monthlyMap.put(m, monthlyMap.get(m) + (b.getCommissionAmount() != null ? b.getCommissionAmount() : 0.0));
        }

        return monthlyMap.entrySet().stream()
                .map(e -> new ChartPointDTO(e.getKey().name(), e.getValue()))
                .toList();
    }

    /**
     * Monthly total platform revenue (all payments)
     */
    public List<ChartPointDTO> monthlyTotalRevenue() {
        List<Booking> paidBookings = bookingRepo.findAll()
                .stream()
                .filter(b -> b.getPaymentStatus() == PaymentStatus.PAID && b.getPaymentDate() != null)
                .toList();

        Map<Month, Double> monthlyMap = new LinkedHashMap<>();

        for (Month month : Month.values()) {
            monthlyMap.put(month, 0.0);
        }

        for (Booking b : paidBookings) {
            Month m = b.getPaymentDate().getMonth();
            monthlyMap.put(m, monthlyMap.get(m) + (b.getTotalPrice() != null ? b.getTotalPrice() : 0.0));
        }

        return monthlyMap.entrySet().stream()
                .map(e -> new ChartPointDTO(e.getKey().name(), e.getValue()))
                .toList();
    }

    /**
     * Monthly bookings count for platform
     */
    public List<ChartPointDTO> monthlyBookingsCount() {
        List<Booking> paidBookings = bookingRepo.findAll()
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
     * Revenue by owner (top owners)
     */
    public List<ChartPointDTO> revenueByOwner() {
        List<Booking> paidBookings = bookingRepo.findAll()
                .stream()
                .filter(b -> b.getPaymentStatus() == PaymentStatus.PAID)
                .toList();

        Map<String, Double> ownerMap = paidBookings.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getBillboard().getOwner().getName(),
                        Collectors.summingDouble(b -> b.getCommissionAmount() != null ? b.getCommissionAmount() : 0.0)
                ));

        return ownerMap.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(10)
                .map(e -> new ChartPointDTO(e.getKey(), e.getValue()))
                .toList();
    }

    /**
     * Total platform statistics
     */
    public Map<String, Object> getPlatformStats() {
        List<Booking> allBookings = bookingRepo.findAll();
        List<Booking> paidBookings = allBookings.stream()
                .filter(b -> b.getPaymentStatus() == PaymentStatus.PAID)
                .toList();

        double totalRevenue = paidBookings.stream()
                .mapToDouble(b -> b.getTotalPrice() != null ? b.getTotalPrice() : 0.0)
                .sum();

        double totalCommission = paidBookings.stream()
                .mapToDouble(b -> b.getCommissionAmount() != null ? b.getCommissionAmount() : 0.0)
                .sum();

        double totalGst = paidBookings.stream()
                .mapToDouble(b -> b.getGstAmount() != null ? b.getGstAmount() : 0.0)
                .sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalCommission", totalCommission);
        stats.put("totalGst", totalGst);
        stats.put("totalBookings", paidBookings.size());
        stats.put("pendingBookings", allBookings.stream().filter(b -> b.getPaymentStatus() == PaymentStatus.PENDING).count());

        return stats;
    }
}

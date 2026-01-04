package com.billboarding.Controller.Admin;

import com.billboarding.ENUM.BookingStatus;
import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Entity.OWNER.Billboard;
import com.billboarding.Entity.User;
import com.billboarding.Repository.BillBoard.BillboardRepository;
import com.billboarding.Repository.Booking.BookingRepository;
import com.billboarding.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Admin Analytics Controller - provides chart data and detailed analytics
 * All endpoints require ADMIN role
 */
@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnalyticsController {

    private final BookingRepository bookingRepository;
    private final BillboardRepository billboardRepository;
    private final UserRepository userRepository;

    /**
     * Get monthly revenue data for charts
     * GET /api/admin/analytics/revenue-chart
     */
    @GetMapping("/revenue-chart")
    public ResponseEntity<List<Map<String, Object>>> getRevenueChart() {
        List<Booking> allBookings = bookingRepository.findAllWithDetails();

        // Get last 6 months
        LocalDate now = LocalDate.now();
        List<Map<String, Object>> chartData = new ArrayList<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM");

        for (int i = 5; i >= 0; i--) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
            String monthLabel = monthStart.format(monthFormatter);

            // Calculate revenue for this month (approved bookings only)
            double monthRevenue = allBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.APPROVED)
                .filter(b -> {
                    LocalDate bookingDate = b.getCreatedAt().toLocalDate();
                    return !bookingDate.isBefore(monthStart) && !bookingDate.isAfter(monthEnd);
                })
                .mapToDouble(Booking::getTotalPrice)
                .sum();

            Map<String, Object> point = new HashMap<>();
            point.put("label", monthLabel);
            point.put("value", monthRevenue);
            chartData.add(point);
        }

        return ResponseEntity.ok(chartData);
    }

    /**
     * Get monthly bookings count for charts
     * GET /api/admin/analytics/bookings-chart
     */
    @GetMapping("/bookings-chart")
    public ResponseEntity<List<Map<String, Object>>> getBookingsChart() {
        List<Booking> allBookings = bookingRepository.findAllWithDetails();

        LocalDate now = LocalDate.now();
        List<Map<String, Object>> chartData = new ArrayList<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM");

        for (int i = 5; i >= 0; i--) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
            String monthLabel = monthStart.format(monthFormatter);

            long monthBookings = allBookings.stream()
                .filter(b -> {
                    LocalDate bookingDate = b.getCreatedAt().toLocalDate();
                    return !bookingDate.isBefore(monthStart) && !bookingDate.isAfter(monthEnd);
                })
                .count();

            Map<String, Object> point = new HashMap<>();
            point.put("label", monthLabel);
            point.put("value", monthBookings);
            chartData.add(point);
        }

        return ResponseEntity.ok(chartData);
    }

    /**
     * Get top cities by billboard count
     * GET /api/admin/analytics/top-cities
     */
    @GetMapping("/top-cities")
    public ResponseEntity<List<Map<String, Object>>> getTopCities() {
        List<Billboard> allBillboards = billboardRepository.findAll();

        // Group by city and count
        Map<String, Long> cityCount = allBillboards.stream()
            .filter(b -> b.getLocation() != null && !b.getLocation().isEmpty())
            .collect(Collectors.groupingBy(
                b -> extractCity(b.getLocation()),
                Collectors.counting()
            ));

        // Sort by count descending and take top 6
        List<Map<String, Object>> topCities = cityCount.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(6)
            .map(entry -> {
                Map<String, Object> city = new HashMap<>();
                city.put("city", entry.getKey());
                city.put("count", entry.getValue());
                return city;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(topCities);
    }

    /**
     * Get billboard type distribution
     * GET /api/admin/analytics/billboard-types
     */
    @GetMapping("/billboard-types")
    public ResponseEntity<List<Map<String, Object>>> getBillboardTypes() {
        List<Billboard> allBillboards = billboardRepository.findAll();
        long total = allBillboards.size();

        if (total == 0) {
            return ResponseEntity.ok(Arrays.asList(
                createTypeEntry("Digital", 0),
                createTypeEntry("Static", 0),
                createTypeEntry("LED", 0),
                createTypeEntry("Neon", 0)
            ));
        }

        // Group by type and calculate percentage
        Map<String, Long> typeCount = allBillboards.stream()
            .filter(b -> b.getType() != null)
            .collect(Collectors.groupingBy(
                b -> b.getType().toString(),
                Collectors.counting()
            ));

        List<Map<String, Object>> typeDistribution = new ArrayList<>();
        for (Map.Entry<String, Long> entry : typeCount.entrySet()) {
            int percentage = (int) Math.round((entry.getValue() * 100.0) / total);
            typeDistribution.add(createTypeEntry(entry.getKey(), percentage));
        }

        // Sort by percentage descending
        typeDistribution.sort((a, b) ->
            Integer.compare((int) b.get("demand"), (int) a.get("demand")));

        return ResponseEntity.ok(typeDistribution);
    }

    /**
     * Get top advertisers by spend
     * GET /api/admin/analytics/top-advertisers
     */
    @GetMapping("/top-advertisers")
    public ResponseEntity<List<Map<String, Object>>> getTopAdvertisers() {
        List<Booking> allBookings = bookingRepository.findAllWithDetails();

        // Group by advertiser and calculate totals
        Map<Long, AdvertiserStats> advertiserStats = new HashMap<>();

        for (Booking booking : allBookings) {
            if (booking.getAdvertiser() != null &&
                booking.getStatus() == BookingStatus.APPROVED) {

                Long advertiserId = booking.getAdvertiser().getId();
                AdvertiserStats stats = advertiserStats.computeIfAbsent(
                    advertiserId,
                    k -> new AdvertiserStats(
                        booking.getAdvertiser().getName(),
                        booking.getAdvertiser().getEmail()
                    )
                );
                stats.addBooking(booking.getTotalPrice());
            }
        }

        // Sort by spent and take top 5
        List<Map<String, Object>> topAdvertisers = advertiserStats.values().stream()
            .sorted(Comparator.comparingDouble(AdvertiserStats::getSpent).reversed())
            .limit(5)
            .map(stats -> {
                Map<String, Object> advertiser = new HashMap<>();
                advertiser.put("name", stats.getName());
                advertiser.put("email", stats.getEmail());
                advertiser.put("bookings", stats.getBookings());
                advertiser.put("spent", stats.getSpent());
                return advertiser;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(topAdvertisers);
    }

    /**
     * Get platform commission stats
     * GET /api/admin/analytics/platform-stats
     */
    @GetMapping("/platform-stats")
    public ResponseEntity<Map<String, Object>> getPlatformStats() {
        List<Booking> approvedBookings = bookingRepository.findByStatusWithDetails(BookingStatus.APPROVED);

        double totalRevenue = approvedBookings.stream()
            .mapToDouble(Booking::getTotalPrice)
            .sum();

        // Calculate commission from commissionAmount field or estimate
        double totalCommission = approvedBookings.stream()
            .mapToDouble(b -> {
                // Commission is stored in commissionAmount field if available
                Double commission = b.getCommissionAmount();
                if (commission != null && commission > 0) {
                    return commission;
                }
                // Fallback: estimate 15% of base (before 18% GST)
                double baseWithCommission = b.getTotalPrice() / 1.18;
                return baseWithCommission * 0.15 / 1.15;
            })
            .sum();

        // GST is 18% of taxable value
        double totalGst = approvedBookings.stream()
            .mapToDouble(b -> {
                Double gst = b.getGstAmount();
                if (gst != null && gst > 0) {
                    return gst;
                }
                return b.getTotalPrice() - (b.getTotalPrice() / 1.18);
            })
            .sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalCommission", totalCommission);
        stats.put("totalGst", totalGst);
        stats.put("totalBookings", approvedBookings.size());
        stats.put("pendingBookings", bookingRepository.countByStatus(BookingStatus.PENDING));

        return ResponseEntity.ok(stats);
    }

    // Helper methods
    private String extractCity(String location) {
        if (location == null || location.isEmpty()) {
            return "Unknown";
        }
        // Try to extract city from location string (usually last part after comma)
        String[] parts = location.split(",");
        String city = parts[parts.length - 1].trim();
        return city.isEmpty() ? "Unknown" : city;
    }

    private Map<String, Object> createTypeEntry(String type, int demand) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("type", type);
        entry.put("demand", demand);
        return entry;
    }

    // Inner class for advertiser stats aggregation
    private static class AdvertiserStats {
        private final String name;
        private final String email;
        private int bookings = 0;
        private double spent = 0;

        public AdvertiserStats(String name, String email) {
            this.name = name != null ? name : "Unknown";
            this.email = email != null ? email : "";
        }

        public void addBooking(double amount) {
            this.bookings++;
            this.spent += amount;
        }

        public String getName() { return name; }
        public String getEmail() { return email; }
        public int getBookings() { return bookings; }
        public double getSpent() { return spent; }
    }
}

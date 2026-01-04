package com.billboarding.Controller.Admin.Dashboard;

import com.billboarding.DTO.ChartPointDTO;
import com.billboarding.Services.Admin.Dashboard.AdminDashboardChartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard/charts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardChartController {

    private final AdminDashboardChartService chartService;

    @GetMapping("/commission")
    public ResponseEntity<List<ChartPointDTO>> getMonthlyCommission() {
        return ResponseEntity.ok(chartService.monthlyCommissionRevenue());
    }

    @GetMapping("/revenue")
    public ResponseEntity<List<ChartPointDTO>> getMonthlyRevenue() {
        return ResponseEntity.ok(chartService.monthlyTotalRevenue());
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<ChartPointDTO>> getMonthlyBookings() {
        return ResponseEntity.ok(chartService.monthlyBookingsCount());
    }

    @GetMapping("/owner-revenue")
    public ResponseEntity<List<ChartPointDTO>> getRevenueByOwner() {
        return ResponseEntity.ok(chartService.revenueByOwner());
    }

    @GetMapping("/platform-stats")
    public ResponseEntity<Map<String, Object>> getPlatformStats() {
        return ResponseEntity.ok(chartService.getPlatformStats());
    }
}

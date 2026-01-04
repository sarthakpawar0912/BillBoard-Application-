package com.billboarding.Controller.Owner.Dashboard;

import com.billboarding.DTO.ChartPointDTO;
import com.billboarding.Entity.User;
import com.billboarding.Services.Owner.Dashboard.OwnerDashboardChartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/owner/dashboard/charts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
public class OwnerDashboardChartController {

    private final OwnerDashboardChartService chartService;

    @GetMapping("/revenue")
    public ResponseEntity<List<ChartPointDTO>> getMonthlyRevenue(Authentication auth) {
        User owner = (User) auth.getPrincipal();
        return ResponseEntity.ok(chartService.monthlyRevenue(owner));
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<ChartPointDTO>> getMonthlyBookings(Authentication auth) {
        User owner = (User) auth.getPrincipal();
        return ResponseEntity.ok(chartService.monthlyBookingsCount(owner));
    }

    @GetMapping("/billboard-revenue")
    public ResponseEntity<List<ChartPointDTO>> getRevenueByBillboard(Authentication auth) {
        User owner = (User) auth.getPrincipal();
        return ResponseEntity.ok(chartService.revenueByBillboard(owner));
    }
}

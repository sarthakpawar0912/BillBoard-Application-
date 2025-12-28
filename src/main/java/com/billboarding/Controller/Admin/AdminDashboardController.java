package com.billboarding.Controller.Admin;

import com.billboarding.Entity.Admin.AdminDashboardResponse;
import com.billboarding.Services.Admin.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;


    // NEW PATH → No more conflict
    @GetMapping("/stats")
    public ResponseEntity<AdminDashboardResponse> getDashboardStats() {
        return ResponseEntity.ok(dashboardService.getDashboardData());
    }
}

package com.billboarding.Controller.Owner;

import com.billboarding.DTO.OWNER.OwnerAnalyticsResponse;
import com.billboarding.DTO.OWNER.OwnerDashboardResponse;
import com.billboarding.Entity.User;
import com.billboarding.Services.Owner.OwnerAnalyticsService;
import com.billboarding.Services.Owner.OwnerDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/owner/dashboard")
@RequiredArgsConstructor
public class OwnerDashboardController {

    private final OwnerDashboardService dashboardService;
    private final OwnerAnalyticsService ownerAnalyticsService;
    @GetMapping
    public ResponseEntity<OwnerDashboardResponse> getDashboard(Authentication auth) {
        User owner = (User) auth.getPrincipal();
        return ResponseEntity.ok(dashboardService.getDashboard(owner));
    }

}
package com.billboarding.Controller.Advertiser;

import com.billboarding.DTO.Advertiser.AdvertiserDashboardResponse;
import com.billboarding.Entity.User;
import com.billboarding.Services.Advertiser.AdvertiserDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/advertiser/dashboards")
@RequiredArgsConstructor
public class AdvertiserDashboardController {

    private final AdvertiserDashboardService dashboardService;

    @GetMapping
    public ResponseEntity<AdvertiserDashboardResponse> getDashboard(Authentication auth) {
        User advertiser = (User) auth.getPrincipal();
        return ResponseEntity.ok(dashboardService.getDashboard(advertiser));
    }
}

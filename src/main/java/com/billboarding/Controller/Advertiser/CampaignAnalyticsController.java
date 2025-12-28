package com.billboarding.Controller.Advertiser;
import com.billboarding.Entity.User;
import com.billboarding.Services.Advertiser.CampaignAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/advertiser/campaigns/analytics")
@RequiredArgsConstructor
@CrossOrigin
public class CampaignAnalyticsController {

    private final CampaignAnalyticsService analyticsService;

    @GetMapping("/{campaignId}")
    public ResponseEntity<?> analytics(
            @PathVariable Long campaignId,
            Authentication auth
    ) {
        User advertiser = (User) auth.getPrincipal();
        return ResponseEntity.ok(
                analyticsService.getAnalytics(campaignId, advertiser)
        );
    }
}

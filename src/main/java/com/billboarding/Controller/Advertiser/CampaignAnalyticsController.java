package com.billboarding.Controller.Advertiser;

import com.billboarding.Entity.User;
import com.billboarding.Services.Advertiser.CampaignAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/advertiser/campaigns")
@RequiredArgsConstructor
@CrossOrigin
public class CampaignAnalyticsController {

    private final CampaignAnalyticsService service;

    // 📊 CAMPAIGN OVERALL ANALYTICS
    @GetMapping("/{campaignId}/analytics")
    public Object analytics(
            @PathVariable Long campaignId,
            Authentication auth
    ) {
        return service.analytics(
                campaignId,
                (User) auth.getPrincipal()
        );
    }
}

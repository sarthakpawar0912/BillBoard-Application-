package com.billboarding.Controller.Advertiser;

import com.billboarding.DTO.Advertiser.CampaignDTO;
import com.billboarding.Entity.Advertiser.Campaign;
import com.billboarding.Entity.User;
import com.billboarding.Services.Advertiser.CampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/advertiser/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService service;

    @PostMapping
    public Campaign create(@RequestBody CampaignDTO dto, Authentication auth) {
        return service.create((User) auth.getPrincipal(), dto);
    }

    @GetMapping
    public List<Campaign> list(Authentication auth) {
        return service.getMyCampaigns((User) auth.getPrincipal());
    }

    @PatchMapping("/{id}/pause")
    public Campaign pause(@PathVariable Long id) {
        return service.pause(id);
    }

    @PatchMapping("/{id}/resume")
    public Campaign resume(@PathVariable Long id) {
        return service.resume(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}

package com.billboarding.Controller.Notofication;

import com.billboarding.DTO.AdvertiserNotificationDTO;
import com.billboarding.Entity.User;
import com.billboarding.Services.Notification.AdvertiserNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/advertiser/settings/notifications")
@RequiredArgsConstructor
public class AdvertiserNotificationController {

    private final AdvertiserNotificationService service;

    // ======================
    // GET NOTIFICATION SETTINGS
    // ======================
    @GetMapping
    public ResponseEntity<?> getSettings(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(service.getSettings(user));
    }

    // ======================
    // UPDATE SETTINGS
    // ======================
    @PostMapping
    public ResponseEntity<?> updateSettings(
            @RequestBody AdvertiserNotificationDTO dto,
            Authentication auth
    ) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(service.saveSettings(user, dto));
    }
}

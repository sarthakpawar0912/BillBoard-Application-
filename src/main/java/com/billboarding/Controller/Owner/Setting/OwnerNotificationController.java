package com.billboarding.Controller.Owner.Setting;

import com.billboarding.Entity.OWNER.Notification.OwnerNotificationSettings;
import com.billboarding.Entity.User;
import com.billboarding.Services.Owner.Notification.OwnerNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/owner/settings/notifications")
@RequiredArgsConstructor
public class OwnerNotificationController {

    private final OwnerNotificationService service;

    @GetMapping
    public ResponseEntity<?> get(Authentication auth) {
        User owner = (User) auth.getPrincipal();
        return ResponseEntity.ok(service.get(owner));
    }

    @PutMapping
    public ResponseEntity<?> update(
            @RequestBody OwnerNotificationSettings req,
            Authentication auth
    ) {
        User owner = (User) auth.getPrincipal();
        return ResponseEntity.ok(service.update(owner, req));
    }
}

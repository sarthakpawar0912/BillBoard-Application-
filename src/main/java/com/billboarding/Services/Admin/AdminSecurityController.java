package com.billboarding.Services.Admin;

import com.billboarding.DTO.ChangePasswordRequest;
import com.billboarding.Entity.User;
import com.billboarding.Services.security.AdminSecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/security")
@RequiredArgsConstructor
public class AdminSecurityController {

    private final AdminSecurityService service;

    // ============ Platform Security Settings ============

    /**
     * Get platform security settings (2FA statistics)
     */
    @GetMapping("/settings")
    public ResponseEntity<?> getSecuritySettings() {
        return ResponseEntity.ok(service.getSecuritySettings());
    }

    @PostMapping("/force-2fa")
    public ResponseEntity<?> force2FA() {
        service.force2FAForAll();
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "2FA enforced for all users"
        ));
    }

    @PostMapping("/disable-force-2fa")
    public ResponseEntity<?> disableForce2FA() {
        service.disableForce2FAForAll();
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Forced 2FA disabled for all users"
        ));
    }

    // ============ User-specific 2FA Management ============

    /**
     * Get user's 2FA status
     */
    @GetMapping("/users/{userId}/2fa-status")
    public ResponseEntity<?> getUserTwoFactorStatus(@PathVariable Long userId) {
        User user = service.getUserById(userId);

        Map<String, Object> status = new HashMap<>();
        status.put("userId", user.getId());
        status.put("email", user.getEmail());
        status.put("name", user.getName());
        status.put("twoFactorEnabled", user.isTwoFactorEnabled());
        status.put("twoFactorMethod", user.getTwoFactorMethod());
        status.put("forceTwoFactor", user.isForceTwoFactor());

        return ResponseEntity.ok(status);
    }

    /**
     * Enforce 2FA for a specific user
     */
    @PostMapping("/users/{userId}/enforce-2fa")
    public ResponseEntity<?> enforceTwoFactorForUser(@PathVariable Long userId) {
        service.enforceTwoFactorForUser(userId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "2FA enforcement enabled for user"
        ));
    }

    /**
     * Remove 2FA enforcement for a specific user (allows them to disable 2FA)
     */
    @PostMapping("/users/{userId}/remove-force-2fa")
    public ResponseEntity<?> removeForceTwoFactor(@PathVariable Long userId) {
        service.removeForceTwoFactor(userId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "2FA enforcement removed for user"
        ));
    }

    /**
     * Completely disable 2FA for a user (admin override)
     */
    @PostMapping("/users/{userId}/disable-2fa")
    public ResponseEntity<?> disableTwoFactorForUser(@PathVariable Long userId) {
        service.disableTwoFactorForUser(userId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "2FA completely disabled for user"
        ));
    }

    /**
     * Reset 2FA for a user (clears current setup, requires re-setup)
     */
    @PostMapping("/users/{userId}/reset-2fa")
    public ResponseEntity<?> resetTwoFactorForUser(@PathVariable Long userId) {
        service.resetTwoFactorForUser(userId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "2FA reset for user - they will need to set up 2FA again"
        ));
    }

    // ============ Admin's Own Security Settings ============

    /**
     * Get admin's login history
     */
    @GetMapping("/login-history")
    public ResponseEntity<?> getAdminLoginHistory(Authentication auth) {
        User admin = (User) auth.getPrincipal();
        return ResponseEntity.ok(service.getAdminLoginHistory(admin.getEmail()));
    }

    /**
     * Change admin's password
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changeAdminPassword(
            @RequestBody ChangePasswordRequest request,
            Authentication auth
    ) {
        User admin = (User) auth.getPrincipal();
        try {
            service.changeAdminPassword(admin, request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Password changed successfully"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}

package com.billboarding.Controller.Security;

import com.billboarding.DTO.ChangePasswordRequest;
import com.billboarding.DTO.SecuritySettingsResponse;
import com.billboarding.DTO.TwoFactorSetupRequest;
import com.billboarding.Entity.User;
import com.billboarding.ENUM.UserRole;
import com.billboarding.Repository.Security.RecoveryCodeRepository;
import com.billboarding.Services.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/settings/security")
@RequiredArgsConstructor
public class SecurityControllers {

    private final SecurityService securityService;
    private final RecoveryCodeRepository recoveryCodeRepository;

    // ============================
    // GET SECURITY SETTINGS (OWNER + ADVERTISER)
    // ============================
    @GetMapping
    public ResponseEntity<?> getSecuritySettings(Authentication auth) {
        User user = (User) auth.getPrincipal();

        if (user.getRole() == UserRole.ADMIN) {
            return ResponseEntity
                    .status(403)
                    .body("Admin cannot access user security settings");
        }

        // Check if user has recovery codes
        boolean hasRecoveryCodes = !recoveryCodeRepository
                .findByEmailAndUsedFalse(user.getEmail())
                .isEmpty();

        SecuritySettingsResponse response = SecuritySettingsResponse.builder()
                .twoFactorEnabled(user.isTwoFactorEnabled())
                .twoFactorMethod(user.getTwoFactorMethod())
                .forceTwoFactor(user.isForceTwoFactor())
                .hasRecoveryCodes(hasRecoveryCodes)
                .adminEnforced2FA(user.isForceTwoFactor())
                .build();

        return ResponseEntity.ok(response);
    }

    // ============================
    // CHANGE PASSWORD (OWNER + ADVERTISER)
    // ============================
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest req,
            Authentication auth
    ) {
        User user = (User) auth.getPrincipal();

        if (user.getRole() == UserRole.ADMIN) {
            return ResponseEntity
                    .status(403)
                    .body("Admin cannot change password here");
        }

        securityService.changePassword(user, req);
        return ResponseEntity.ok("Password updated successfully");
    }

    // ============================
    // SET / UPDATE 2FA METHOD
    // ============================
    @PostMapping("/2fa")
    public ResponseEntity<?> set2FA(
            @RequestBody TwoFactorSetupRequest req,
            Authentication auth
    ) {
        User user = (User) auth.getPrincipal();

        if (user.getRole() == UserRole.ADMIN) {
            return ResponseEntity
                    .status(403)
                    .body("Admin cannot change 2FA settings");
        }

        securityService.updateTwoFactor(user, req.getMethod());
        return ResponseEntity.ok("2FA updated to " + req.getMethod());
    }

    // ============================
    // LOGIN HISTORY
    // ============================
    @GetMapping("/login-history")
    public ResponseEntity<?> loginHistory(Authentication auth) {
        User user = (User) auth.getPrincipal();

        if (user.getRole() == UserRole.ADMIN) {
            return ResponseEntity
                    .status(403)
                    .body("Admin has no personal login history");
        }

        return ResponseEntity.ok(
                securityService.getLoginHistory(user)
        );
    }
}

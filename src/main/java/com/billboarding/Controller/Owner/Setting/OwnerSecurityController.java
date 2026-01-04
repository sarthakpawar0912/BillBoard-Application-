package com.billboarding.Controller.Owner.Setting;
import com.billboarding.DTO.ChangePasswordRequest;
import com.billboarding.DTO.TwoFactorDTO;
import com.billboarding.ENUM.UserRole;
import com.billboarding.Entity.User;
import com.billboarding.Services.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/owner/settings/security")
@RequiredArgsConstructor
public class OwnerSecurityController {

    private final SecurityService securityService;

    // Change password
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest req,
            Authentication auth
    ) {
        User user = (User) auth.getPrincipal();

        if (user.getRole() != UserRole.OWNER) {
            return ResponseEntity.status(403).body("Access denied");
        }

        securityService.changePassword(user, req);
        return ResponseEntity.ok("Password updated successfully");
    }

    // Toggle 2FA
    @PostMapping("/2fa")
    public ResponseEntity<?> toggle2FA(
            @RequestBody TwoFactorDTO dto,
            Authentication auth
    ) {
        User user = (User) auth.getPrincipal();

        if (user.getRole() != UserRole.OWNER) {
            return ResponseEntity.status(403).body("Access denied");
        }

        securityService.toggle2FA(user, dto.isEnabled());
        return ResponseEntity.ok("2FA updated");
    }

    // Login history
    @GetMapping("/login-history")
    public ResponseEntity<?> loginHistory(Authentication auth) {
        User user = (User) auth.getPrincipal();

        if (user.getRole() != UserRole.OWNER) {
            return ResponseEntity.status(403).body("Access denied");
        }

        return ResponseEntity.ok(
                securityService.getLoginHistory(user)
        );
    }
}

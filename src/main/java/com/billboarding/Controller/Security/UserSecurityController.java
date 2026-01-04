package com.billboarding.Controller.Security;
import com.billboarding.Entity.User;
import com.billboarding.Services.security.SecurityService;
import com.billboarding.Services.security.TwoFactorResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user/security")
@RequiredArgsConstructor
public class UserSecurityController {

    private final SecurityService securityService;

    @PostMapping("/2fa/disable")
    public ResponseEntity<?> disable2FA(Authentication auth) {
        User user = (User) auth.getPrincipal();

        try {
            securityService.disableAll2FA(user);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "2FA disabled. Direct login enabled."
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}

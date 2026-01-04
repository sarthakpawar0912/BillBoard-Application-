package com.billboarding.Controller.Security;
import com.billboarding.DTO.AdminUnlockRequest;
import com.billboarding.ENUM.UserRole;
import com.billboarding.Entity.User;
import com.billboarding.Services.security.AdminSecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/admin/security")
@RequiredArgsConstructor
public class AdminSecurityControllers {

    private final AdminSecurityService adminSecurityService;

    @PostMapping("/unlock-user")
    public ResponseEntity<?> unlockUser(
            @RequestBody AdminUnlockRequest req,
            Authentication auth
    ) {
        User admin = (User) auth.getPrincipal();

        if (admin.getRole() != UserRole.ADMIN) {
            return ResponseEntity.status(403).body("Access denied");
        }

        adminSecurityService.unlockUser(req.getEmail());
        return ResponseEntity.ok("User account unlocked");
    }
}

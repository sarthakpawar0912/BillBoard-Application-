package com.billboarding.Controller.Security;

import com.billboarding.DTO.EmailDTO;
import com.billboarding.DTO.TokenDTO;
import com.billboarding.Services.security.TwoFactorResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/security/2fa")
@RequiredArgsConstructor
public class TwoFactorResetController {

    private final TwoFactorResetService resetService;

    @PostMapping("/reset-request")
    public ResponseEntity<?> requestReset(@RequestBody EmailDTO dto) {
        resetService.sendResetEmail(dto.getEmail());
        return ResponseEntity.ok("2FA reset email sent");
    }

    @PostMapping("/reset-confirm")
    public ResponseEntity<?> confirmReset(@RequestBody TokenDTO dto) {
        resetService.confirmReset(dto.getToken());
        return ResponseEntity.ok("2FA reset successful");
    }
}

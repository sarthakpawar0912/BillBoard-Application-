package com.billboarding.Controller.Security;

import com.billboarding.Services.security.RecoveryCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/security/recovery")
@RequiredArgsConstructor
public class RecoveryController {

    private final RecoveryCodeService service;

    @PostMapping("/generate")
    public ResponseEntity<List<String>> generate(Authentication auth) {
        return ResponseEntity.ok(
                service.generate(auth.getName())
        );
    }
}

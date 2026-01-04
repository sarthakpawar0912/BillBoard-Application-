package com.billboarding.Controller.AUTH;

import com.billboarding.DTO.*;
import com.billboarding.Entity.User;
import com.billboarding.Exception.ResourceNotFoundException;
import com.billboarding.Exception.BusinessException;
import com.billboarding.Services.Auth.AuthService;
import com.billboarding.Services.UserService;
import com.billboarding.Services.security.MagicLinkService;
import com.billboarding.Services.security.RecoveryCodeService;
import com.billboarding.Services.security.TwoFactorService;
import com.billboarding.Services.JWT.JwtService;
import com.billboarding.Repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final JwtService jwtService;
    private final TwoFactorService twoFactorService; // EMAIL OTP
    private final UserRepository userRepository;
    private final RecoveryCodeService recoveryCodeService;
    private final MagicLinkService magicLinkService; // MAGIC LINK

    // ============================
    // REGISTER
    // ============================
    @PostMapping("/register")
    public ResponseEntity<User> register(
            @RequestBody @Valid RegisterRequest request
    ) {
        return ResponseEntity.ok(userService.register(request));
    }

    // ============================
    // LOGIN (STEP 1)
    // ============================
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        // AuthService already contains switch logic
        return ResponseEntity.ok(
                authService.login(request, httpRequest)
        );
    }

    // ============================
    // VERIFY EMAIL OTP (STEP 2A)
    // ============================
    @PostMapping("/verify-email-otp")
    public ResponseEntity<AuthResponse> verifyEmailOtp(
            @RequestBody OTPVerifyRequest req
    ) {
        twoFactorService.verifyOTP(req.getEmail(), req.getOtp());
        return issueJwt(req.getEmail());
    }

    // ============================
    // RESEND EMAIL OTP
    // ============================
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(
            @RequestBody OTPResendRequest req
    ) {
        twoFactorService.sendOtp(req.getEmail());
        return ResponseEntity.ok("OTP resent successfully");
    }

    // ============================
    // JWT ISSUER (PRIVATE HELPER)
    // ============================
    private ResponseEntity<AuthResponse> issueJwt(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        return ResponseEntity.ok(
                new AuthResponse(
                        false,                  // twoFactorRequired
                        token,
                        user.getRole().name(),
                        user.getId(),
                        "Login successful"
                )
        );
    }
    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(
            @RequestBody @Valid OTPVerifyRequest req
    ) {

        twoFactorService.verifyOTP(req.getEmail(), req.getOtp());

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", req.getEmail()));

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        return ResponseEntity.ok(
                new AuthResponse(
                        false,
                        token,
                        user.getRole().name(),
                        user.getId(),
                        "Login successful"
                )
        );
    }

    @PostMapping("/verify-recovery")
    public ResponseEntity<AuthResponse> verifyRecovery(@RequestBody @Valid RecoveryVerifyRequest req) {

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", req.getEmail()));

        if (!user.isTwoFactorEnabled()) {
            throw new BusinessException("Recovery codes not enabled for this account");
        }

        recoveryCodeService.verify(req.getEmail(), req.getCode());
        return issueJwt(req.getEmail());
    }

    // ============================
    // MAGIC LINK - REQUEST
    // ============================
    @PostMapping("/magic-link/request")
    public ResponseEntity<?> requestMagicLink(@RequestBody MagicLinkRequest req) {
        magicLinkService.sendMagicLink(req.getEmail());
        return ResponseEntity.ok(
                java.util.Map.of(
                        "success", true,
                        "message", "Magic link sent to your email"
                )
        );
    }

    // ============================
    // MAGIC LINK - VERIFY
    // ============================
    @PostMapping("/magic-link/verify")
    public ResponseEntity<AuthResponse> verifyMagicLink(@RequestBody MagicLinkVerifyRequest req) {
        return ResponseEntity.ok(magicLinkService.verifyMagicLink(req.getToken()));
    }
}

package com.billboarding.Controller.AUTH;

import com.billboarding.DTO.AuthResponse;
import com.billboarding.DTO.LoginRequest;
import com.billboarding.DTO.OTPVerifyRequest;
import com.billboarding.DTO.RegisterRequest;
import com.billboarding.Entity.User;
import com.billboarding.Services.Auth.AuthService;
import com.billboarding.Services.UserService;
import com.billboarding.Services.security.TwoFactorService;
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
    private final com.billboarding.Services.JWT.JwtService jwtService;
    private final TwoFactorService twoFactorService;
    private final com.billboarding.Repository.UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody @Valid RegisterRequest request) {
        User saved = userService.register(request);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(
                authService.login(request, httpRequest)
        );
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(
            @RequestBody OTPVerifyRequest req
    ) {
        twoFactorService.verifyOTP(req.getEmail(), req.getOtp());

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

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

}


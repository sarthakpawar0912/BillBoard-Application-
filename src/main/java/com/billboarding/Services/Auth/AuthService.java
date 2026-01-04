package com.billboarding.Services.Auth;

import com.billboarding.DTO.AuthResponse;
import com.billboarding.DTO.LoginRequest;
import com.billboarding.Entity.Security.LoginHistory;
import com.billboarding.Entity.User;
import com.billboarding.ENUM.TwoFactorMethod;
import com.billboarding.Repository.Security.LoginHistoryRepository;
import com.billboarding.Repository.UserRepository;
import com.billboarding.Services.JWT.JwtService;
import com.billboarding.Services.security.LoginAttemptService;
import com.billboarding.Services.security.LoginRiskService;
import com.billboarding.Services.security.MagicLinkService;
import com.billboarding.Services.security.TwoFactorService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authManager;
    private final UserRepository userRepo;
    private final JwtService jwtService;
    private final TwoFactorService twoFactorService;
    private final LoginAttemptService loginAttemptService;
    private final LoginRiskService loginRiskService;
    private final LoginHistoryRepository loginHistoryRepo;
    private final MagicLinkService magicLinkService;

    public AuthResponse login(LoginRequest request, HttpServletRequest http) {

        loginAttemptService.checkIfBlocked(request.getEmail());

        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            loginAttemptService.loginFailed(request.getEmail());
            throw new RuntimeException("Invalid credentials");
        }

        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        loginAttemptService.loginSuccess(user.getEmail());

        boolean risky = loginRiskService.isRisky(
                user.getEmail(),
                http.getRemoteAddr(),
                http.getHeader("User-Agent")
        );

        loginHistoryRepo.save(
                LoginHistory.builder()
                        .email(user.getEmail())
                        .ip(http.getRemoteAddr())
                        .userAgent(http.getHeader("User-Agent"))
                        .loginAt(LocalDateTime.now())
                        .risky(risky)
                        .build()
        );

        boolean require2FA =
                user.isTwoFactorEnabled() ||
                        user.isForceTwoFactor() ||
                        risky;

        if (!require2FA) {
            return issueJwt(user);
        }

        // 🔐 RISK ONLY → EMAIL OTP
        if (risky && user.getTwoFactorMethod() == TwoFactorMethod.NONE) {
            twoFactorService.sendOtp(user.getEmail());
            return pending2FA(user, "Email OTP required");
        }

        return switch (user.getTwoFactorMethod()) {

            case EMAIL_OTP -> {
                twoFactorService.sendOtp(user.getEmail());
                yield pending2FA(user, "Email OTP required");
            }

            case MAGIC_LINK -> {
                magicLinkService.sendMagicLink(user.getEmail());
                yield pending2FA(user, "Magic link sent to your email. Click to login.");
            }

            default -> issueJwt(user);
        };
    }

    private AuthResponse issueJwt(User user) {
        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole().name()
        );
        return new AuthResponse(false, token, user.getRole().name(), user.getId(), "Login successful");
    }

    private AuthResponse pending2FA(User user, String msg) {
        return new AuthResponse(true, null, user.getRole().name(), user.getId(), msg);
    }
}

package com.billboarding.Services.Auth;

import com.billboarding.DTO.AuthResponse;
import com.billboarding.DTO.LoginRequest;
import com.billboarding.Entity.Security.LoginHistory;
import com.billboarding.Entity.User;
import com.billboarding.Repository.Security.LoginHistoryRepository;
import com.billboarding.Repository.UserRepository;
import com.billboarding.Services.JWT.JwtService;

import com.billboarding.Services.security.TwoFactorService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final LoginHistoryRepository loginHistoryRepository;
    private final TwoFactorService twoFactorService;

    public AuthResponse login(
            LoginRequest request,
            HttpServletRequest httpRequest
    ) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.isBlocked()) {
            throw new RuntimeException("Your account is blocked");
        }

        // ✅ Save login history
        loginHistoryRepository.save(
                LoginHistory.builder()
                        .email(user.getEmail())
                        .ipAddress(httpRequest.getRemoteAddr())
                        .userAgent(httpRequest.getHeader("User-Agent"))
                        .loginAt(LocalDateTime.now())
                        .build()
        );

        // 🔐 2FA CHECK
        if (user.isTwoFactorEnabled()) {
            twoFactorService.sendOTP(user.getEmail());

            return new AuthResponse(
                    true,
                    null,
                    user.getRole().name(),
                    user.getId(),
                    "OTP sent to email"
                        );
        }

        // 🔓 No 2FA → issue JWT
        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        return new AuthResponse(
                false,
                token,
                user.getRole().name(),
                user.getId(),
                "Login successful"
                );
    }
}

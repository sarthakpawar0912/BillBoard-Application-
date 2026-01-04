package com.billboarding.Services.security;

import com.billboarding.DTO.AuthResponse;
import com.billboarding.Entity.Security.MagicLinkToken;
import com.billboarding.Entity.User;
import com.billboarding.Repository.Security.MagicLinkRepository;
import com.billboarding.Repository.UserRepository;
import com.billboarding.Services.JWT.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class MagicLinkService {

    private final MagicLinkRepository magicLinkRepo;
    private final UserRepository userRepo;
    private final EmailService emailService;
    private final JwtService jwtService;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    private static final int TOKEN_VALIDITY_MINUTES = 15;
    private static final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public void sendMagicLink(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isBlocked()) {
            throw new RuntimeException("Account is blocked");
        }

        // Delete any existing unused tokens for this email
        magicLinkRepo.deleteByEmail(email);

        // Generate secure token
        String token = generateSecureToken();

        // Save token
        MagicLinkToken magicToken = MagicLinkToken.builder()
                .email(email)
                .token(token)
                .expiresAt(LocalDateTime.now().plusMinutes(TOKEN_VALIDITY_MINUTES))
                .build();

        magicLinkRepo.save(magicToken);

        // Build magic link URL
        String magicLink = frontendUrl + "/auth/magic-link?token=" + token;

        // Send email
        String subject = "Your Magic Login Link - Billboarding App";
        String body = buildEmailBody(user.getName(), magicLink);

        emailService.send(email, subject, body);
    }

    @Transactional
    public AuthResponse verifyMagicLink(String token) {
        MagicLinkToken magicToken = magicLinkRepo.findByTokenAndUsedFalse(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired magic link"));

        if (magicToken.isExpired()) {
            throw new RuntimeException("Magic link has expired. Please request a new one.");
        }

        if (magicToken.isUsed()) {
            throw new RuntimeException("Magic link has already been used.");
        }

        // Mark token as used
        magicToken.setUsed(true);
        magicLinkRepo.save(magicToken);

        // Get user and issue JWT
        User user = userRepo.findByEmail(magicToken.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isBlocked()) {
            throw new RuntimeException("Account is blocked");
        }

        String jwt = jwtService.generateToken(user.getEmail(), user.getRole().name());

        return new AuthResponse(false, jwt, user.getRole().name(), user.getId(), "Login successful via magic link");
    }

    private String generateSecureToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private String buildEmailBody(String name, String magicLink) {
        return String.format("""
            Hi %s,

            You requested a magic login link for your Billboarding App account.

            Click the link below to log in (valid for %d minutes):

            %s

            If you didn't request this, you can safely ignore this email.

            Security Tip: Never share this link with anyone.

            Best regards,
            Billboarding App Team
            """, name, TOKEN_VALIDITY_MINUTES, magicLink);
    }

    // Cleanup expired tokens (can be called by scheduled task)
    @Transactional
    public void cleanupExpiredTokens() {
        magicLinkRepo.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}

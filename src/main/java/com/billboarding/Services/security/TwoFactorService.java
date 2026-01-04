package com.billboarding.Services.security;
import com.billboarding.Entity.Security.TwoFactorOTP;
import com.billboarding.Repository.Security.TwoFactorOTPRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;@Service
@RequiredArgsConstructor
public class TwoFactorService {

    private final TwoFactorOTPRepository repo;
    private final EmailService emailService;
    private final PasswordEncoder encoder;

    public void sendOtp(String email) {

        repo.findTopByEmailOrderByExpiresAtDesc(email).ifPresent(old -> {
            if (old.getLastSentAt().isAfter(LocalDateTime.now().minusSeconds(60))) {
                throw new RuntimeException("Wait before requesting OTP again");
            }
            repo.delete(old);
        });

        String rawOtp = String.valueOf(100000 + new SecureRandom().nextInt(900000));

        repo.save(
                TwoFactorOTP.builder()
                        .email(email)
                        .otpHash(encoder.encode(rawOtp))
                        .expiresAt(LocalDateTime.now().plusMinutes(5))
                        .lastSentAt(LocalDateTime.now())
                        .build()
        );

        emailService.send(email, "Login OTP", "OTP: " + rawOtp);
    }

    public void verifyOTP(String email, String otp) {

        TwoFactorOTP record = repo.findTopByEmailOrderByExpiresAtDesc(email)
                .orElseThrow(() -> new RuntimeException("OTP not found"));

        if (record.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new RuntimeException("OTP expired");

        if (!encoder.matches(otp, record.getOtpHash()))
            throw new RuntimeException("Invalid OTP");

        repo.delete(record);
    }
}

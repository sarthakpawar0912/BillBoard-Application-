package com.billboarding.Services.security;
import com.billboarding.Entity.Security.TwoFactorOTP;
import com.billboarding.Repository.Security.TwoFactorOTPRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
public class TwoFactorService {

    private final TwoFactorOTPRepository otpRepo;
    private final EmailService emailService;

    public void sendOTP(String email) {

        String otp = String.valueOf(100000 + new SecureRandom().nextInt(900000));

        otpRepo.save(
                TwoFactorOTP.builder()
                        .email(email)
                        .otp(otp)
                        .expiresAt(LocalDateTime.now().plusMinutes(5))
                        .build()
        );

        emailService.send(
                email,
                "Your Login OTP",
                "Your OTP is: " + otp + " (valid for 5 minutes)"
        );
    }

    public void verifyOTP(String email, String otp) {

        TwoFactorOTP record = otpRepo.findTopByEmailOrderByExpiresAtDesc(email)
                .orElseThrow(() -> new RuntimeException("OTP not found"));

        if (record.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        if (!record.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }
    }
}

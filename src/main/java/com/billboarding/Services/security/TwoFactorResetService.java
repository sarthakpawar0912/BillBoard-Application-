package com.billboarding.Services.security;

import com.billboarding.ENUM.TwoFactorMethod;
import com.billboarding.Entity.Security.TwoFactorResetToken;
import com.billboarding.Entity.User;
import com.billboarding.Notification.EmailNotificationService;
import com.billboarding.Repository.Security.MagicLinkRepository;
import com.billboarding.Repository.Security.TwoFactorResetRepository;
import com.billboarding.Repository.Security.RecoveryCodeRepository;
import com.billboarding.Repository.Security.TwoFactorOTPRepository;
import com.billboarding.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TwoFactorResetService {

    private final TwoFactorResetRepository repo;
    private final EmailNotificationService emailService;
    private final UserRepository userRepo;
    private final RecoveryCodeRepository recoveryCodeRepo;
    private final TwoFactorOTPRepository twoFactorOTPRepo;
    private final MagicLinkRepository magicLinkRepo;

    public void sendResetEmail(String email) {

        String token = UUID.randomUUID().toString();

        repo.save(
                TwoFactorResetToken.builder()
                        .email(email)
                        .token(token)
                        .expiresAt(LocalDateTime.now().plusMinutes(15))
                        .build()
        );

        emailService.sendEmail(
                email,
                "2FA Reset Request",
                "Use this token to reset 2FA:\n\n" + token
        );
    }

    @Transactional
    public void confirmReset(String token) {

        TwoFactorResetToken record = repo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        if (record.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token expired");
        }

        User user = userRepo.findByEmail(record.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Reset 2FA state
        user.setTwoFactorEnabled(false);
        user.setTwoFactorMethod(TwoFactorMethod.NONE);
        userRepo.save(user);

        // Remove all 2FA secrets
        recoveryCodeRepo.deleteByEmail(user.getEmail());
        twoFactorOTPRepo.deleteByEmail(user.getEmail());
        magicLinkRepo.deleteByEmail(user.getEmail());

        repo.delete(record);
    }
}

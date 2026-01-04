package com.billboarding.Services.security;

import com.billboarding.DTO.ChangePasswordRequest;
import com.billboarding.ENUM.TwoFactorMethod;
import com.billboarding.Entity.Security.LoginHistory;
import com.billboarding.Entity.User;
import com.billboarding.Repository.Security.LoginHistoryRepository;
import com.billboarding.Repository.Security.MagicLinkRepository;
import com.billboarding.Repository.Security.RecoveryCodeRepository;
import com.billboarding.Repository.Security.TwoFactorOTPRepository;
import com.billboarding.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final LoginHistoryRepository loginRepo;
    private final RecoveryCodeRepository recoveryCodeRepository;
    private final TwoFactorOTPRepository twoFactorOTPRepository;
    private final MagicLinkRepository magicLinkRepository;

    public void updateTwoFactor(User user, TwoFactorMethod method) {

        // Admin enforced → cannot disable
        if (user.isForceTwoFactor() && method == TwoFactorMethod.NONE) {
            throw new RuntimeException("Admin enforced 2FA. Cannot disable.");
        }

        user.setTwoFactorMethod(method);
        user.setTwoFactorEnabled(method != TwoFactorMethod.NONE);

        userRepo.save(user);
    }

    @Transactional
    public void disableAll2FA(User user) {

        if (user.isForceTwoFactor()) {
            throw new RuntimeException("Admin enforced 2FA. Cannot disable.");
        }

        user.setTwoFactorEnabled(false);
        user.setTwoFactorMethod(TwoFactorMethod.NONE);
        userRepo.save(user);

        // Clear all 2FA secrets
        recoveryCodeRepository.deleteByEmail(user.getEmail());
        twoFactorOTPRepository.deleteByEmail(user.getEmail());
        magicLinkRepository.deleteByEmail(user.getEmail());
    }



    // 🔑 Change Password
    public void changePassword(User user, ChangePasswordRequest req) {

        if (!encoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password incorrect");
        }

        user.setPassword(encoder.encode(req.getNewPassword()));
        userRepo.save(user);
    }

    // 🔐 Toggle 2FA
    public void toggle2FA(User user, boolean enabled) {
        user.setTwoFactorEnabled(enabled);
        userRepo.save(user);
    }

    // 🕒 Login history
    public List<LoginHistory> getLoginHistory(User user) {
        return loginRepo.findByEmailOrderByLoginAtDesc(user.getEmail());
    }
}

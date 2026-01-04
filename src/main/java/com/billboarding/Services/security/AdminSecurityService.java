package com.billboarding.Services.security;
import com.billboarding.ENUM.TwoFactorMethod;
import com.billboarding.ENUM.UserRole;
import com.billboarding.Entity.Security.LoginAttempt;
import com.billboarding.Entity.Security.LoginHistory;
import com.billboarding.Entity.User;
import com.billboarding.Repository.Security.LoginAttemptRepository;
import com.billboarding.Repository.Security.LoginHistoryRepository;
import com.billboarding.Repository.Security.MagicLinkRepository;
import com.billboarding.Repository.Security.RecoveryCodeRepository;
import com.billboarding.Repository.Security.TwoFactorOTPRepository;
import com.billboarding.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminSecurityService {

    private final LoginAttemptRepository loginAttemptRepo;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final RecoveryCodeRepository recoveryCodeRepository;
    private final TwoFactorOTPRepository twoFactorOTPRepository;
    private final MagicLinkRepository magicLinkRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    public void unlockUser(String email) {

        // remove login attempt record
        loginAttemptRepo.findByEmail(email)
                .ifPresent(loginAttemptRepo::delete);

        // also unblock user if permanently blocked
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setBlocked(false);
        userRepository.save(user);

        emailService.send(
                email,
                "Account Unlocked",
                "Your account has been unlocked by admin. You may login again."
        );
    }


    public void force2FAForAll() {
        userRepository.findAll().forEach(u -> {
            u.setForceTwoFactor(true);
            userRepository.save(u);
        });
    }

    // ============ NEW: Admin 2FA Management for Individual Users ============

    /**
     * Disable 2FA enforcement for a specific user (allows them to disable 2FA themselves)
     */
    public void removeForceTwoFactor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setForceTwoFactor(false);
        userRepository.save(user);

        emailService.send(
                user.getEmail(),
                "2FA Requirement Removed",
                "The administrator has removed the mandatory 2FA requirement for your account. " +
                "You can now manage your 2FA settings freely."
        );
    }

    /**
     * Completely disable 2FA for a user (admin override)
     */
    @Transactional
    public void disableTwoFactorForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Remove force flag and disable 2FA
        user.setForceTwoFactor(false);
        user.setTwoFactorEnabled(false);
        user.setTwoFactorMethod(TwoFactorMethod.NONE);
        userRepository.save(user);

        // Clear all 2FA secrets
        recoveryCodeRepository.deleteByEmail(user.getEmail());
        twoFactorOTPRepository.deleteByEmail(user.getEmail());
        magicLinkRepository.deleteByEmail(user.getEmail());

        emailService.send(
                user.getEmail(),
                "2FA Disabled by Administrator",
                "Your two-factor authentication has been disabled by an administrator. " +
                "You can re-enable it from your security settings if needed."
        );
    }

    /**
     * Reset 2FA for a user (disable current and allow re-setup)
     */
    @Transactional
    public void resetTwoFactorForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Keep force flag but reset method
        user.setTwoFactorEnabled(false);
        user.setTwoFactorMethod(TwoFactorMethod.NONE);
        userRepository.save(user);

        // Clear all 2FA secrets
        recoveryCodeRepository.deleteByEmail(user.getEmail());
        twoFactorOTPRepository.deleteByEmail(user.getEmail());
        magicLinkRepository.deleteByEmail(user.getEmail());

        emailService.send(
                user.getEmail(),
                "2FA Reset by Administrator",
                "Your two-factor authentication has been reset by an administrator. " +
                "Please set up 2FA again from your security settings."
        );
    }

    /**
     * Enforce 2FA for a specific user
     */
    public void enforceTwoFactorForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setForceTwoFactor(true);
        userRepository.save(user);

        emailService.send(
                user.getEmail(),
                "2FA Now Required",
                "An administrator has enabled mandatory two-factor authentication for your account. " +
                "Please set up 2FA from your security settings."
        );
    }

    /**
     * Get user 2FA status
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ============ Admin Security Settings ============

    /**
     * Get platform security settings statistics
     */
    public Map<String, Object> getSecuritySettings() {
        List<User> allUsers = userRepository.findAll();

        // Filter non-admin users
        List<User> nonAdminUsers = allUsers.stream()
                .filter(u -> u.getRole() != UserRole.ADMIN)
                .toList();

        long totalUsersWithout2FA = nonAdminUsers.stream()
                .filter(u -> !u.isTwoFactorEnabled())
                .count();

        long totalUsersWithEmailOTP = nonAdminUsers.stream()
                .filter(u -> u.getTwoFactorMethod() == TwoFactorMethod.EMAIL_OTP)
                .count();

        long totalUsersWithMagicLink = nonAdminUsers.stream()
                .filter(u -> u.getTwoFactorMethod() == TwoFactorMethod.MAGIC_LINK)
                .count();

        long totalUsersWithForce2FA = nonAdminUsers.stream()
                .filter(User::isForceTwoFactor)
                .count();

        // Check if force 2FA is globally enabled (all non-admin users have force2FA)
        boolean force2FAEnabled = !nonAdminUsers.isEmpty() &&
                nonAdminUsers.stream().allMatch(User::isForceTwoFactor);

        Map<String, Object> settings = new HashMap<>();
        settings.put("force2FAEnabled", force2FAEnabled);
        settings.put("totalUsersWithout2FA", totalUsersWithout2FA);
        settings.put("totalUsersWithEmailOTP", totalUsersWithEmailOTP);
        settings.put("totalUsersWithMagicLink", totalUsersWithMagicLink);
        settings.put("totalUsersWithForce2FA", totalUsersWithForce2FA);
        settings.put("totalUsers", nonAdminUsers.size());

        return settings;
    }

    /**
     * Disable forced 2FA for all users
     */
    public void disableForce2FAForAll() {
        userRepository.findAll().forEach(u -> {
            if (u.getRole() != UserRole.ADMIN) {
                u.setForceTwoFactor(false);
                userRepository.save(u);
            }
        });
    }

    /**
     * Get login history for admin user
     */
    public List<LoginHistory> getAdminLoginHistory(String email) {
        return loginHistoryRepository.findByEmailOrderByLoginAtDesc(email);
    }

    /**
     * Change admin password
     */
    public void changeAdminPassword(User admin, String oldPassword, String newPassword) {
        // Verify old password
        if (!passwordEncoder.matches(oldPassword, admin.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Validate new password
        if (newPassword == null || newPassword.length() < 8) {
            throw new RuntimeException("New password must be at least 8 characters");
        }

        // Update password
        admin.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(admin);
    }
}

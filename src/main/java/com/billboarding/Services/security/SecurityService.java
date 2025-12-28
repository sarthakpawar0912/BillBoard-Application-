package com.billboarding.Services.security;

import com.billboarding.DTO.ChangePasswordRequest;
import com.billboarding.Entity.Security.LoginHistory;
import com.billboarding.Entity.User;
import com.billboarding.Repository.Security.LoginHistoryRepository;
import com.billboarding.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final LoginHistoryRepository loginRepo;

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

package com.billboarding.Services;

import com.billboarding.DTO.ChangePasswordRequest;
import com.billboarding.Entity.User;
import com.billboarding.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdvertiserSecurityService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void changePassword(User user, ChangePasswordRequest req) {

        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password incorrect");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }

    public void deleteAccount(User user) {
        user.setBlocked(true); // soft delete
        userRepository.save(user);
    }
}

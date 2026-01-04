package com.billboarding.Services;

import com.billboarding.DTO.RegisterRequest;
import com.billboarding.ENUM.KycStatus;
import com.billboarding.ENUM.UserRole;
import com.billboarding.Entity.User;
import com.billboarding.Notification.EmailNotificationService;
import com.billboarding.Repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailNotificationService emailService;

    public User register(RegisterRequest request) {

        // ⭐ Role already comes as ENUM from RegisterRequest
        UserRole finalRole = request.getRole();

        // 🔥 RULE 1 — Only one ADMIN allowed
        if (finalRole == UserRole.ADMIN) {
            boolean adminExists = userRepository.existsByRole(UserRole.ADMIN);
            if (adminExists) {
                throw new RuntimeException("Admin already exists! Only one admin allowed in the system.");
            }
        }

        // 🔥 RULE 2 — Check unique email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User already registered with this email");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(finalRole);  // ⭐ Correct
        user.setKycStatus(KycStatus.PENDING);
        user.setBlocked(false);

        User saved = userRepository.save(user);

        emailService.sendRegistrationEmail(saved);

        return saved;
    }
    // 🔹 Self email update (OWNER / ADVERTISER / ADMIN)
    public User updateEmail(User user, String newEmail) {

        if (userRepository.existsByEmail(newEmail)) {
            throw new RuntimeException("Email already in use");
        }

        user.setEmail(newEmail);
        return userRepository.save(user);
    }

    // 🔹 Admin email update
    public User adminUpdateEmail(Long userId, String newEmail) {

        if (userRepository.existsByEmail(newEmail)) {
            throw new RuntimeException("Email already in use");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEmail(newEmail);
        return userRepository.save(user);
    }

}

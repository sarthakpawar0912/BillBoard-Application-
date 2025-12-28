package com.billboarding.Services.Admin;


import com.billboarding.ENUM.KycStatus;
import com.billboarding.ENUM.UserRole;
import com.billboarding.Entity.User;
import com.billboarding.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AdminService:
 *  - Manage users (view, KYC, block/unblock)
 */
@Service
@RequiredArgsConstructor
public class AdminsService {

    private final UserRepository userRepository;

    /**
     * 🔹 Get all users in system
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * 🔹 Get users whose KYC is PENDING
     */
    public List<User> getPendingKycUsers() {
        return userRepository.findByKycStatus(KycStatus.PENDING);
    }

    /**
     * 🔹 Update KYC status for a user (APPROVED / REJECTED)
     */
    public User updateKycStatus(Long userId, KycStatus status) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Safety: don't touch admin KYC
        if (user.getRole() == UserRole.ADMIN) {
            throw new RuntimeException("Cannot update KYC of ADMIN");
        }

        user.setKycStatus(status);
        return userRepository.save(user);
    }

    /**
     * 🔹 Block a user (cannot log in)
     */
    public User blockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == UserRole.ADMIN) {
            throw new RuntimeException("Cannot block ADMIN user");
        }

        user.setBlocked(true);
        return userRepository.save(user);
    }

    /**
     * 🔹 Unblock a user
     */
    public User unblockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setBlocked(false);
        return userRepository.save(user);
    }
}

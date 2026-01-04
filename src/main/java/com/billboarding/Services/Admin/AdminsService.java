package com.billboarding.Services.Admin;


import com.billboarding.DTO.ADMIN.OwnerStatsResponse;
import com.billboarding.ENUM.KycStatus;
import com.billboarding.ENUM.UserRole;
import com.billboarding.Entity.OWNER.wallet.OwnerWallet;
import com.billboarding.Entity.User;
import com.billboarding.Repository.BillBoard.BillboardRepository;
import com.billboarding.Repository.Owner.Wallet.OwnerWalletRepository;
import com.billboarding.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AdminService:
 *  - Manage users (view, KYC, block/unblock)
 */
@Service
@RequiredArgsConstructor
public class AdminsService {

    private final UserRepository userRepository;
    private final BillboardRepository billboardRepository;
    private final OwnerWalletRepository ownerWalletRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

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

    /**
     * 🔹 Get all owners with aggregated stats (billboard count, earnings)
     * Used by Admin Owners page to show accurate data.
     */
    public List<OwnerStatsResponse> getAllOwnersWithStats() {
        // Get all users with OWNER role
        List<User> owners = userRepository.findByRole(UserRole.OWNER);

        return owners.stream()
                .map(this::mapToOwnerStats)
                .collect(Collectors.toList());
    }

    /**
     * Maps a User entity to OwnerStatsResponse with aggregated data.
     */
    private OwnerStatsResponse mapToOwnerStats(User owner) {
        // Count billboards owned by this user
        long billboardCount = billboardRepository.countByOwner(owner);

        // Get total earnings from wallet (totalEarned field)
        double totalEarnings = ownerWalletRepository.findByOwner(owner)
                .map(OwnerWallet::getTotalEarned)
                .orElse(0.0);

        // Handle null totalEarned
        if (totalEarnings < 0) {
            totalEarnings = 0.0;
        }

        return OwnerStatsResponse.builder()
                .id(owner.getId())
                .name(owner.getName())
                .email(owner.getEmail())
                .phone(owner.getPhone())
                .role(owner.getRole().name())
                .kycStatus(owner.getKycStatus().name())
                .blocked(owner.isBlocked())
                .createdAt(owner.getCreatedAt() != null
                        ? owner.getCreatedAt().format(DATE_FORMATTER)
                        : null)
                .billboardCount((int) billboardCount)
                .totalEarnings(totalEarnings)
                .company(null) // Can be fetched from OwnerProfile if needed
                .build();
    }
}

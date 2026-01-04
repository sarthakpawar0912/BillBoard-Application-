package com.billboarding.Repository;

import com.billboarding.ENUM.KycStatus;
import com.billboarding.ENUM.UserRole;
import com.billboarding.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository layer for User entity.
 * Extends JpaRepository → gives us CRUD methods like save(), findById(), findAll(), etc.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by email (used in login & registration duplicate check)
    Optional<User> findByEmail(String email);

    // Check if email already exists in database
    boolean existsByRole(UserRole role);

    boolean existsByEmail(String email);

    List<User> findByKycStatus(KycStatus status);

    // 🔹 For admin filters
    List<User> findByBlockedTrue();
    List<User> findByRole(UserRole role);

    // ===== OPTIMIZED COUNT QUERIES =====
    long countByRole(UserRole role);
    long countByKycStatus(KycStatus status);
    long countByBlockedTrue();

    // ===== PAGINATED QUERIES =====
    Page<User> findByRole(UserRole role, Pageable pageable);
    Page<User> findByKycStatus(KycStatus status, Pageable pageable);
    Page<User> findByBlockedTrue(Pageable pageable);
}
package com.billboarding.Repository.Security;
import com.billboarding.Entity.Security.TwoFactorOTP;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TwoFactorOTPRepository
        extends JpaRepository<TwoFactorOTP, Long> {

    Optional<TwoFactorOTP> findTopByEmailOrderByExpiresAtDesc(String email);
}

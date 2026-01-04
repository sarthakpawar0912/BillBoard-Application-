package com.billboarding.Repository.Security;

import com.billboarding.Entity.Security.TwoFactorResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TwoFactorResetRepository
        extends JpaRepository<TwoFactorResetToken, Long> {

    Optional<TwoFactorResetToken> findByToken(String token);
}

package com.billboarding.Repository.Security;

import com.billboarding.Entity.Security.MagicLinkToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MagicLinkRepository extends JpaRepository<MagicLinkToken, Long> {

    Optional<MagicLinkToken> findByTokenAndUsedFalse(String token);

    Optional<MagicLinkToken> findByEmailAndUsedFalseAndExpiresAtAfter(String email, LocalDateTime now);

    void deleteByEmail(String email);

    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}

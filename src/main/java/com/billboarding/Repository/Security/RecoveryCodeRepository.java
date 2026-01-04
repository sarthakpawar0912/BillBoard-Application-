package com.billboarding.Repository.Security;

import com.billboarding.Entity.Security.RecoveryCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecoveryCodeRepository extends JpaRepository<RecoveryCode, Long> {

    List<RecoveryCode> findByEmailAndUsedFalse(String email);

    Optional<RecoveryCode> findByEmailAndCodeHashAndUsedFalse(String email, String codeHash);

    void deleteByEmail(String email);
}

package com.billboarding.Repository.Security;

import com.billboarding.Entity.Security.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginAttemptRepository
        extends JpaRepository<LoginAttempt, Long> {

    Optional<LoginAttempt> findByEmail(String email);
}

package com.billboarding.Services.security;

import com.billboarding.Entity.Security.LoginAttempt;
import com.billboarding.Repository.Security.LoginAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final LoginAttemptRepository repo;
    private final EmailService emailService;

    public void loginFailed(String email) {

        LoginAttempt attempt = repo.findByEmail(email)
                .orElse(LoginAttempt.builder()
                        .email(email)
                        .failedAttempts(0)
                        .build());

        attempt.setFailedAttempts(attempt.getFailedAttempts() + 1);

        int fails = attempt.getFailedAttempts();
        LocalDateTime now = LocalDateTime.now();

        if (fails == 4) {
            attempt.setLockedUntil(now.plusMinutes(15));
            sendLockMail(email, "15 minutes");
        }
        else if (fails == 5) {
            attempt.setLockedUntil(now.plusMinutes(30));
            sendLockMail(email, "30 minutes");
        }
        else if (fails == 6) {
            attempt.setLockedUntil(now.plusHours(1));
            sendLockMail(email, "1 hour");
        }
        else if (fails >= 7) {
            attempt.setPermanentlyBlocked(true);
            sendPermanentBlockMail(email);
        }

        repo.save(attempt);
    }

    public void loginSuccess(String email) {
        repo.findByEmail(email).ifPresent(repo::delete);
    }

    public void checkIfBlocked(String email) {

        LoginAttempt attempt = repo.findByEmail(email).orElse(null);
        if (attempt == null) return;

        if (attempt.isPermanentlyBlocked()) {
            throw new RuntimeException("Account permanently blocked. Contact admin.");
        }

        if (attempt.getLockedUntil() != null &&
                attempt.getLockedUntil().isAfter(LocalDateTime.now())) {

            throw new RuntimeException(
                    "Account locked until " + attempt.getLockedUntil()
            );
        }
    }

    // 🔔 EMAIL HELPERS

    private void sendLockMail(String email, String duration) {
        emailService.send(
                email,
                "Account Temporarily Locked",
                "Your account has been locked for " + duration +
                        " due to multiple failed login attempts."
        );
    }

    private void sendPermanentBlockMail(String email) {
        emailService.send(
                email,
                "Account Permanently Blocked",
                "Your account has been permanently blocked due to repeated login failures. Please contact admin."
        );
    }
}

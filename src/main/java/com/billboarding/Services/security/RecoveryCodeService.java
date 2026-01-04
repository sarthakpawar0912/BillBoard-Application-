package com.billboarding.Services.security;

import com.billboarding.Entity.Security.RecoveryCode;
import com.billboarding.Repository.Security.RecoveryCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecoveryCodeService {

    private final RecoveryCodeRepository repo;
    private final PasswordEncoder encoder;

    @Transactional
    public List<String> generate(String email) {

        repo.deleteByEmail(email); // reset old ones

        return java.util.stream.IntStream.range(0, 8)
                .mapToObj(i -> {
                    String raw = UUID.randomUUID().toString().substring(0, 8);
                    repo.save(
                            RecoveryCode.builder()
                                    .email(email)
                                    .codeHash(encoder.encode(raw))
                                    .used(false)
                                    .build()
                    );
                    return raw;
                })
                .toList();
    }

    public void verify(String email, String code) {

        List<RecoveryCode> codes = repo.findByEmailAndUsedFalse(email);

        RecoveryCode match = codes.stream()
                .filter(c -> encoder.matches(code, c.getCodeHash()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid recovery code"));

        match.setUsed(true);
        repo.save(match);
    }
}

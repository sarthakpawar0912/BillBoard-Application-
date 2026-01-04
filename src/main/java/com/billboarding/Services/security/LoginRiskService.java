package com.billboarding.Services.security;

import com.billboarding.Repository.Security.LoginHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginRiskService {

    private final LoginHistoryRepository repo;

    public boolean isRisky(String email, String ip, String agent) {

        return repo.findTop5ByEmailOrderByLoginAtDesc(email)
                .stream()
                .noneMatch(h ->
                        h.getIp().equals(ip) &&
                        h.getUserAgent().equals(agent)
                );
    }
}

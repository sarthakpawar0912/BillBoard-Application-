package com.billboarding.Repository.Security;


import com.billboarding.Entity.Security.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoginHistoryRepository
        extends JpaRepository<LoginHistory, Long> {

    List<LoginHistory> findByEmailOrderByLoginAtDesc(String email);
}

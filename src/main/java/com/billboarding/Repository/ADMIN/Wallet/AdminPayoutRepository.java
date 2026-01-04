package com.billboarding.Repository.Admin.Wallet;

import com.billboarding.Entity.ADMIN.wallet.AdminPayout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdminPayoutRepository extends JpaRepository<AdminPayout, Long> {

    List<AdminPayout> findAllByOrderByInitiatedAtDesc();

    List<AdminPayout> findByStatusOrderByInitiatedAtDesc(String status);

    List<AdminPayout> findByInitiatedAtBetweenOrderByInitiatedAtDesc(LocalDateTime start, LocalDateTime end);
}

package com.billboarding.Repository.Owner.Wallet;

import com.billboarding.Entity.OWNER.wallet.PayoutRequest;
import com.billboarding.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayoutRequestRepository extends JpaRepository<PayoutRequest, Long> {

    List<PayoutRequest> findByOwner(User owner);

    List<PayoutRequest> findByOwnerOrderByCreatedAtDesc(User owner);

    List<PayoutRequest> findByStatus(String status);

    List<PayoutRequest> findAllByOrderByCreatedAtDesc();
}

package com.billboarding.Repository.Payment;
import com.billboarding.Entity.Payment.PaymentMethod;
import com.billboarding.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    List<PaymentMethod> findByUser(User user);

    Optional<PaymentMethod> findByUserAndIsDefaultTrue(User user);
}

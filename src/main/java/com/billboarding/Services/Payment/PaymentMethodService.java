package com.billboarding.Services.Payment;

import com.billboarding.Entity.Payment.PaymentMethod;
import com.billboarding.Entity.User;
import com.billboarding.Repository.Payment.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class PaymentMethodService {

    private final PaymentMethodRepository repo;

    public List<PaymentMethod> getMyMethods(User user) {
        return repo.findByUser(user);
    }

    public PaymentMethod addMethod(User user, String type, String label) {

        // Remove default if first method
        if (repo.findByUser(user).isEmpty()) {
            return repo.save(
                    PaymentMethod.builder()
                            .user(user)
                            .type(type)
                            .label(label)
                            .isDefault(true)
                            .build()
            );
        }

        return repo.save(
                PaymentMethod.builder()
                        .user(user)
                        .type(type)
                        .label(label)
                        .isDefault(false)
                        .build()
        );
    }

    public void setDefault(User user, Long id) {
        repo.findByUser(user).forEach(m -> {
            m.setDefault(m.getId().equals(id));
            repo.save(m);
        });
    }

    public void remove(User user, Long id) {
        PaymentMethod pm = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Method not found"));
        if (!pm.getUser().getId().equals(user.getId()))
            throw new RuntimeException("Not yours");

        repo.delete(pm);
    }
}

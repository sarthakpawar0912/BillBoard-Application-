package com.billboarding.Services.Owner.Notification;

import com.billboarding.Entity.OWNER.Notification.OwnerNotificationSettings;
import com.billboarding.Entity.User;
import com.billboarding.Repository.Owner.Notification.OwnerNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class OwnerNotificationService {

    private final OwnerNotificationRepository repo;

    public OwnerNotificationSettings get(User owner) {
        return repo.findByOwner(owner)
                .orElse(
                        repo.save(
                                OwnerNotificationSettings.builder()
                                        .owner(owner)
                                        .bookingRequests(true)
                                        .paymentUpdates(true)
                                        .marketing(false)
                                        .smsAlerts(true)
                                        .build()
                        )
                );
    }

    public OwnerNotificationSettings update(
            User owner,
            OwnerNotificationSettings updated
    ) {
        OwnerNotificationSettings existing = get(owner);

        existing.setBookingRequests(updated.isBookingRequests());
        existing.setPaymentUpdates(updated.isPaymentUpdates());
        existing.setMarketing(updated.isMarketing());
        existing.setSmsAlerts(updated.isSmsAlerts());

        return repo.save(existing);
    }
}

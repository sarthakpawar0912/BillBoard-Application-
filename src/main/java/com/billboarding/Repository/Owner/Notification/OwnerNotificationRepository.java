package com.billboarding.Repository.Owner.Notification;
import com.billboarding.Entity.OWNER.Notification.OwnerNotificationSettings;
import com.billboarding.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OwnerNotificationRepository
        extends JpaRepository<OwnerNotificationSettings, Long> {

    Optional<OwnerNotificationSettings> findByOwner(User owner);
}

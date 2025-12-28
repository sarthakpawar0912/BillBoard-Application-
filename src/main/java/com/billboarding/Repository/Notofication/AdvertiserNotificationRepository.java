package com.billboarding.Repository.Notofication;

import com.billboarding.Entity.Notification.AdvertiserNotificationSettings;
import com.billboarding.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdvertiserNotificationRepository extends JpaRepository<AdvertiserNotificationSettings, Long> {

    Optional<AdvertiserNotificationSettings> findByAdvertiser(User advertiser);
}

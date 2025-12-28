package com.billboarding.Services.Notification;

import com.billboarding.DTO.AdvertiserNotificationDTO;
import com.billboarding.Entity.Notification.AdvertiserNotificationSettings;
import com.billboarding.Entity.User;
import com.billboarding.Repository.Notofication.AdvertiserNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class AdvertiserNotificationService {

    private final AdvertiserNotificationRepository repo;

    // ======================
    // GET SETTINGS
    // ======================
    public AdvertiserNotificationDTO getSettings(User advertiser) {

        AdvertiserNotificationSettings s =
                repo.findByAdvertiser(advertiser)
                        .orElseGet(() -> defaultSettings(advertiser));

        return mapToDTO(s);
    }

    // ======================
    // SAVE / UPDATE SETTINGS
    // ======================
    public AdvertiserNotificationDTO saveSettings(
            User advertiser,
            AdvertiserNotificationDTO dto
    ) {

        AdvertiserNotificationSettings s =
                repo.findByAdvertiser(advertiser)
                        .orElseGet(() -> defaultSettings(advertiser));

        s.setEmailNotifications(dto.isEmailNotifications());
        s.setSmsNotifications(dto.isSmsNotifications());
        s.setPaymentNotifications(dto.isPaymentNotifications());
        s.setCampaignNotifications(dto.isCampaignNotifications());
        s.setSystemNotifications(dto.isSystemNotifications());

        repo.save(s);
        return mapToDTO(s);
    }

    // ======================
    // HELPERS
    // ======================
    private AdvertiserNotificationSettings defaultSettings(User advertiser) {
        AdvertiserNotificationSettings s =
                AdvertiserNotificationSettings.builder()
                        .advertiser(advertiser)
                        .emailNotifications(true)
                        .smsNotifications(false)
                        .paymentNotifications(true)
                        .campaignNotifications(true)
                        .systemNotifications(true)
                        .build();
        return repo.save(s);
    }

    private AdvertiserNotificationDTO mapToDTO(AdvertiserNotificationSettings s) {
        return AdvertiserNotificationDTO.builder()
                .emailNotifications(s.isEmailNotifications())
                .smsNotifications(s.isSmsNotifications())
                .paymentNotifications(s.isPaymentNotifications())
                .campaignNotifications(s.isCampaignNotifications())
                .systemNotifications(s.isSystemNotifications())
                .build();
    }
}

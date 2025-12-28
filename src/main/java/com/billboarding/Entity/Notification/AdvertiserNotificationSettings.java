package com.billboarding.Entity.Notification;

import com.billboarding.Entity.User;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "advertiser_notification_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdvertiserNotificationSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User advertiser;

    private boolean emailNotifications;
    private boolean smsNotifications;
    private boolean paymentNotifications;
    private boolean campaignNotifications;
    private boolean systemNotifications;
}

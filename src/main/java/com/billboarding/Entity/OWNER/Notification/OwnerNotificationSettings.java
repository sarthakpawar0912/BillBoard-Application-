package com.billboarding.Entity.OWNER.Notification;

import com.billboarding.Entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "owner_notifications")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class OwnerNotificationSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "owner_id", unique = true)
    private User owner;

    private boolean bookingRequests;
    private boolean paymentUpdates;
    private boolean marketing;
    private boolean smsAlerts;
}

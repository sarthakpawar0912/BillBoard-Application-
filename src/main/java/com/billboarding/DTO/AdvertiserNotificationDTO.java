package com.billboarding.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdvertiserNotificationDTO {

    private boolean emailNotifications;
    private boolean smsNotifications;
    private boolean paymentNotifications;
    private boolean campaignNotifications;
    private boolean systemNotifications;
}

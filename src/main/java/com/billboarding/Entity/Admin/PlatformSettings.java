package com.billboarding.Entity.Admin;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "platform_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformSettings {

    @Id
    @Builder.Default
    private Long id = 1L;

    private String platformName;
    private String supportEmail;

    private Double commissionPercent; // 15
    private Double gstPercent;        // 18

    private String currency;          // INR
    private String timezone;          // Asia/Kolkata
}

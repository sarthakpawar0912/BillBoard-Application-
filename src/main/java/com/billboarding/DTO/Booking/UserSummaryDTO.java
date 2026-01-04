package com.billboarding.DTO.Booking;

import com.billboarding.ENUM.KycStatus;
import com.billboarding.ENUM.UserRole;
import com.billboarding.Entity.User;
import lombok.*;

/**
 * Safe user DTO that excludes sensitive data like password
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryDTO {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private UserRole role;
    private KycStatus kycStatus;

    public static UserSummaryDTO fromEntity(User user) {
        if (user == null) return null;

        return UserSummaryDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .kycStatus(user.getKycStatus())
                .build();
    }
}

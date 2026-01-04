package com.billboarding.DTO.User;

import com.billboarding.ENUM.KycStatus;
import com.billboarding.ENUM.TwoFactorMethod;
import com.billboarding.ENUM.UserRole;
import com.billboarding.Entity.User;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User response DTO for admin endpoints - excludes password
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private UserRole role;
    private KycStatus kycStatus;
    private boolean blocked;
    private boolean twoFactorEnabled;
    private TwoFactorMethod twoFactorMethod;
    private LocalDateTime createdAt;

    public static UserResponseDTO fromEntity(User user) {
        if (user == null) return null;

        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .kycStatus(user.getKycStatus())
                .blocked(user.isBlocked())
                .twoFactorEnabled(user.isTwoFactorEnabled())
                .twoFactorMethod(user.getTwoFactorMethod())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public static List<UserResponseDTO> fromEntityList(List<User> users) {
        return users.stream()
                .map(UserResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
}

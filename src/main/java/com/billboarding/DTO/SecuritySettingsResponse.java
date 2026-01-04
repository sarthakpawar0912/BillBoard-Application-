package com.billboarding.DTO;

import com.billboarding.ENUM.TwoFactorMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecuritySettingsResponse {
    private boolean twoFactorEnabled;
    private TwoFactorMethod twoFactorMethod;
    private boolean forceTwoFactor;
    private boolean hasRecoveryCodes;
    private boolean adminEnforced2FA;
}

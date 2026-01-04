package com.billboarding.DTO;

import com.billboarding.ENUM.TwoFactorMethod;
import lombok.Data;

@Data
public class TwoFactorSetupRequest {
    private TwoFactorMethod method; // EMAIL_OTP or GOOGLE_TOTP
}

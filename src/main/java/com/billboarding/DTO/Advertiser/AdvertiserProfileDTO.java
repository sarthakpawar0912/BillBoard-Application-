package com.billboarding.DTO.Advertiser;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdvertiserProfileDTO {

    private String fullName;
    private String email;
    private String phone;

    private String companyName;
    private String industry;
    private String website;

    private boolean hasLogo; // frontend hint
}


package com.billboarding.DTO.OWNER.Profile;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OwnerProfileDTO {

    private String fullName;
    private String email;
    private String phone;

    private String companyName;
    private String gstNumber;
    private String businessAddress;

    private boolean hasLogo;
}

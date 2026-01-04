package com.billboarding.DTO;

import lombok.Data;

@Data
public class RecoveryVerifyRequest {
    private String email;
    private String code;
}

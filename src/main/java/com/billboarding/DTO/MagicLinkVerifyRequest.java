package com.billboarding.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MagicLinkVerifyRequest {

    @NotBlank(message = "Token is required")
    private String token;
}

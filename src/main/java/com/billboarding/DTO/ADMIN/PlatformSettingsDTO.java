package com.billboarding.DTO.ADMIN;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO for updating platform settings.
 * Critical financial fields are validated to prevent invalid configurations.
 */
@Data
public class PlatformSettingsDTO {

    @Size(min = 2, max = 100, message = "Platform name must be between 2 and 100 characters")
    private String platformName;

    @Email(message = "Support email must be a valid email address")
    private String supportEmail;

    @NotNull(message = "Commission percentage is required")
    @DecimalMin(value = "0.0", message = "Commission percentage cannot be negative")
    @DecimalMax(value = "100.0", message = "Commission percentage cannot exceed 100%")
    private Double commissionPercent;

    @NotNull(message = "GST percentage is required")
    @DecimalMin(value = "0.0", message = "GST percentage cannot be negative")
    @DecimalMax(value = "100.0", message = "GST percentage cannot exceed 100%")
    private Double gstPercent;

    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO code (e.g., INR, USD)")
    private String currency;

    private String timezone;
}

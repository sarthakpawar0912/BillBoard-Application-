package com.billboarding.DTO.Advertiser;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for creating a new advertising campaign.
 * Validates all required fields to ensure campaign integrity.
 */
@Data
public class CampaignCreateDTO {

    @NotBlank(message = "Campaign name is required")
    @Size(min = 3, max = 100, message = "Campaign name must be between 3 and 100 characters")
    private String name;

    @NotNull(message = "Budget is required")
    @Positive(message = "Budget must be greater than zero")
    private Double budget;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or in the future")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @FutureOrPresent(message = "End date must be today or in the future")
    private LocalDate endDate;

    @NotEmpty(message = "At least one city must be selected")
    private List<@NotBlank(message = "City name cannot be blank") String> cities;
}

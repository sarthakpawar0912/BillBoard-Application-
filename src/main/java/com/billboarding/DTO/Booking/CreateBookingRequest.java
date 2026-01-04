package com.billboarding.DTO.Booking;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * Request DTO for creating a new booking.
 * All critical fields are validated to prevent null/invalid data.
 */
@Data
public class CreateBookingRequest {

    @NotNull(message = "Billboard ID is required")
    private Long billboardId;

    // campaignId is optional - no validation needed
    private Long campaignId;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or in the future")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @FutureOrPresent(message = "End date must be today or in the future")
    private LocalDate endDate;
}

package com.billboarding.DTO.Booking;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateBookingRequest {

    private Long billboardId;
    private LocalDate startDate;
    private LocalDate endDate;
}

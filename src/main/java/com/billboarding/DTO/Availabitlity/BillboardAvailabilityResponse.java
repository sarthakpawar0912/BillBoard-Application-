package com.billboarding.DTO.Availabitlity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class BillboardAvailabilityResponse {

    private Double price;
    private LocalDate date;
    private String status; // AVAILABLE / BOOKED / PENDING
}

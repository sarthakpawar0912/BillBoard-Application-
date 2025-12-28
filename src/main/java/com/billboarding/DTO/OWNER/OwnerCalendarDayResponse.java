package com.billboarding.DTO.OWNER;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class OwnerCalendarDayResponse {
    private LocalDate date;
    private String status;        // BOOKED / AVAILABLE
    private Double revenue;       // 0 if not booked
}

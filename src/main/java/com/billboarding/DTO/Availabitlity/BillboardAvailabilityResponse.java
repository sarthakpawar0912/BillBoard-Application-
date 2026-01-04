package com.billboarding.DTO.Availabitlity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BillboardAvailabilityResponse {

    private LocalDate date;
    private String status;
    private Double basePrice;
    private Double finalPrice;

    // Frontend expects "price" field - return finalPrice (dynamic price per day)
    @JsonProperty("price")
    public Double getPrice() {
        return finalPrice != null ? finalPrice : basePrice;
    }
}

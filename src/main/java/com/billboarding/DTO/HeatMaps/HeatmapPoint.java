package com.billboarding.DTO.HeatMaps;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HeatmapPoint {
    private Double latitude;
    private Double longitude;
    private Long bookings;
}

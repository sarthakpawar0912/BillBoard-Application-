package com.billboarding.DTO.HeatMaps;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RevenueMapPoint {
    private Double latitude;
    private Double longitude;
    private Double revenue;
}

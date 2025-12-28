package com.billboarding.DTO.OWNER;

import lombok.Data;

@Data
public class HeatmapPointDTO {

    private Double latitude;
    private Double longitude;
    private double intensity;   // revenue at this location
}
package com.billboarding.DTO.OWNER;

import lombok.Data;

@Data
public class BillboardRevenueDTO {

    private Long billboardId;
    private String title;
    private String location;
    private String type;

    private int totalBookings;      // total bookings on this board
    private double totalRevenue;    // only APPROVED bookings
    private int imageCount;         // how many images uploaded

    private Double latitude;        // for map
    private Double longitude;       // for map
}
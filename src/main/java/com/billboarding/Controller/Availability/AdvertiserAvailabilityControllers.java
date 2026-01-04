package com.billboarding.Controller.Availability;


import com.billboarding.DTO.Availabitlity.BillboardAvailabilityResponse;
import com.billboarding.Services.Availability.BillboardAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;@RestController
@RequestMapping("/api/advertiser/availability")
@RequiredArgsConstructor
public class AdvertiserAvailabilityControllers {

    private final BillboardAvailabilityService availabilityService;

    @GetMapping("/calendar/{billboardId}")
    public ResponseEntity<List<BillboardAvailabilityResponse>> getAvailability(
            @PathVariable Long billboardId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate start,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate end
    ) {
        return ResponseEntity.ok(
                availabilityService.getAvailability(billboardId, start, end)
        );
    }
}

package com.billboarding.Controller.Availability;

import com.billboarding.Services.Availability.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/advertiser")
@RequiredArgsConstructor
public class AdvertiserAvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping("/availability/check/{billboardId}")
    public ResponseEntity<Boolean> checkAvailability(
            @PathVariable Long billboardId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,

            Authentication authentication
    ) {
        return ResponseEntity.ok(
                availabilityService.isAvailable(
                        billboardId,
                        date,
                        date
                )
        );
    }
}

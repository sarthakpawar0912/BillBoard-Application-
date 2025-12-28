package com.billboarding.Controller.Availability;

import com.billboarding.Services.Availability.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping("/{billboardId}")
    public ResponseEntity<Boolean> checkAvailability(
            @PathVariable Long billboardId,
            @RequestParam String startDate,
            @RequestParam String endDate
    ) {
        boolean available = availabilityService.isAvailable(
                billboardId,
                LocalDate.parse(startDate),
                LocalDate.parse(endDate)
        );
        return ResponseEntity.ok(available);
    }
}

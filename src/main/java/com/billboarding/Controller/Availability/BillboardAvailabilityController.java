package com.billboarding.Controller.Availability;


import com.billboarding.DTO.Availabitlity.BillboardAvailabilityResponse;
import com.billboarding.Services.Availability.BillboardAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/billboards")
@RequiredArgsConstructor
public class BillboardAvailabilityController {

    private final BillboardAvailabilityService availabilityService;

    /**
     * 📅 Calendar availability API
     */
    @GetMapping("/{billboardId}/availability")
    public List<BillboardAvailabilityResponse> getAvailability(
            @PathVariable Long billboardId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return availabilityService.getAvailability(billboardId, from, to);
    }
}

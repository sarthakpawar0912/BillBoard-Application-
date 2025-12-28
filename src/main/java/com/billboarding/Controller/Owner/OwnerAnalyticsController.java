package com.billboarding.Controller.Owner;

import com.billboarding.DTO.OWNER.OwnerAnalyticsResponse;
import com.billboarding.Entity.OWNER.Billboard;
import com.billboarding.Entity.User;
import com.billboarding.Services.Owner.OwnerAnalyticsService;
import com.billboarding.Services.Owner.OwnerCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
public class OwnerAnalyticsController {

    private final OwnerAnalyticsService analyticsService;
    private final com.billboarding.Repository.BillBoard.BillboardRepository billboardRepo;
    private final OwnerCalendarService calendarService;

    @GetMapping("analytics")
    public ResponseEntity<OwnerAnalyticsResponse> getAnalytics(Authentication auth) {
        User owner = (User) auth.getPrincipal();
        return ResponseEntity.ok(analyticsService.getAnalytics(owner));
    }

    @GetMapping("/calendar/{billboardId}")
    public ResponseEntity<?> calendar(
            @PathVariable Long billboardId,
            @RequestParam LocalDate start,
            @RequestParam LocalDate end,
            Authentication auth
    ) {
        User owner = (User) auth.getPrincipal();
        Billboard board = billboardRepo.findById(billboardId)
                .orElseThrow();

        if (!board.getOwner().getId().equals(owner.getId()))
            return ResponseEntity.status(403).build();

        return ResponseEntity.ok(
                calendarService.getCalendar(board, start, end)
        );
    }

}

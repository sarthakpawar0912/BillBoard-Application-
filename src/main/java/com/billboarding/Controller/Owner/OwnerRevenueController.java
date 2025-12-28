
package com.billboarding.Controller.Owner;

import com.billboarding.DTO.OWNER.OwnerRevenueDashboardResponse;
import com.billboarding.Entity.User;
import com.billboarding.Services.Owner.OwnerRevenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;


@RestController
@RequestMapping("/api/owner/revenue")
@RequiredArgsConstructor
public class OwnerRevenueController {

    private final OwnerRevenueService revenueService;

    @GetMapping("/dashboard")
    public ResponseEntity<OwnerRevenueDashboardResponse> getDashboard(
            Authentication auth,
            @RequestParam(required = false) Long billboardId,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end
    ) {

        User owner = (User) auth.getPrincipal();

        LocalDate s = start != null ? LocalDate.parse(start) : null;
        LocalDate e = end != null ? LocalDate.parse(end) : null;

        return ResponseEntity.ok(
                revenueService.getRevenueDashboard(owner, billboardId, s, e)
        );
    }
    @GetMapping("/monthly")
    public ResponseEntity<?> monthlyRevenue(Authentication auth) {
        User owner = (User) auth.getPrincipal();
        return ResponseEntity.ok(revenueService.getMonthlyRevenue(owner));
    }

}
    /**
     * Returns a rich analytics object:
     *  - summary
     *  - per-billboard revenue stats
     *  - monthly revenue for charts
     *  - heatmap points
     */
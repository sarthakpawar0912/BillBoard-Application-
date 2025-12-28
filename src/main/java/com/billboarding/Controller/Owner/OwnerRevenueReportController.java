package com.billboarding.Controller.Owner;

import com.billboarding.Services.Owner.OwnerRevenueReportService;
import com.billboarding.Entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/owner/revenue/export")
@RequiredArgsConstructor
public class OwnerRevenueReportController {

    private final OwnerRevenueReportService reportService;

    @GetMapping(value = "/csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportRevenueCsv(
            Authentication auth,
            @RequestParam(required = false) Long billboardId,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end
    ) {
        User owner = (User) auth.getPrincipal();

        LocalDate s = start != null ? LocalDate.parse(start) : null;
        LocalDate e = end != null ? LocalDate.parse(end) : null;

        byte[] data = reportService.exportRevenueCsv(owner, billboardId, s, e);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=owner-revenue.csv")
                .body(data);
    }

    @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportRevenuePdf(
            Authentication auth,
            @RequestParam(required = false) Long billboardId,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end
    ) {
        User owner = (User) auth.getPrincipal();

        LocalDate s = start != null ? LocalDate.parse(start) : null;
        LocalDate e = end != null ? LocalDate.parse(end) : null;

        byte[] data = reportService.exportRevenuePdf(owner, billboardId, s, e);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=owner-revenue.pdf")
                .body(data);
    }
}
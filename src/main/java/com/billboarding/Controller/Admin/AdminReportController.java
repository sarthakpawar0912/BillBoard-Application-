package com.billboarding.Controller.Admin;


import com.billboarding.Services.Admin.AdminReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService reportService;

    // ---------- USERS ----------

    @GetMapping(value = "/users/csv", produces = "text/csv")
    public ResponseEntity<byte[]> downloadUsersCsv() {
        byte[] data = reportService.exportUsersCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users-report.csv")
                .body(data);
    }

    @GetMapping(value = "/users/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadUsersPdf() {
        byte[] data = reportService.exportUsersPdf();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users-report.pdf")
                .body(data);
    }

    // ---------- BOOKINGS ----------

    @GetMapping(value = "/bookings/csv", produces = "text/csv")
    public ResponseEntity<byte[]> downloadBookingsCsv() {
        byte[] data = reportService.exportBookingsCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bookings-report.csv")
                .body(data);
    }

    @GetMapping(value = "/bookings/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadBookingsPdf() {
        byte[] data = reportService.exportBookingsPdf();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bookings-report.pdf")
                .body(data);
    }

    // ---------- REVENUE ----------

    @GetMapping(value = "/revenue/csv", produces = "text/csv")
    public ResponseEntity<byte[]> downloadRevenueCsv() {
        byte[] data = reportService.exportRevenueCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=revenue-report.csv")
                .body(data);
    }

    @GetMapping(value = "/revenue/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadRevenuePdf() {
        byte[] data = reportService.exportRevenuePdf();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=revenue-report.pdf")
                .body(data);
    }
}
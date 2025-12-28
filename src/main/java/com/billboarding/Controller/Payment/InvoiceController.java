package com.billboarding.Controller.Payment;

import com.billboarding.Services.Payment.InvoicePdfService;
import com.billboarding.Services.Payment.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final InvoicePdfService pdfService;

    @PostMapping("/{bookingId}")
    public ResponseEntity<byte[]> generate(
            @PathVariable Long bookingId) {
        return ResponseEntity.ok(
                invoiceService.generateInvoice(bookingId)
        );
    }

    @GetMapping("/{bookingId}/pdf")
    public ResponseEntity<byte[]> downloadPdf(
            @PathVariable Long bookingId) {

        byte[] pdf = pdfService.generatePdf(bookingId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=invoice-" + bookingId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}

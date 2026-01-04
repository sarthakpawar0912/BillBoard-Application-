package com.billboarding.Controller.Payment;

import com.billboarding.Entity.Payment.Invoice;
import com.billboarding.Services.Payment.InvoicePdfService;
import com.billboarding.Services.Payment.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final InvoicePdfService pdfService;

    /**
     * Generate invoice and return invoice data (JSON)
     * POST /api/invoices/{bookingId}
     */
    @PostMapping("/{bookingId}")
    public ResponseEntity<Invoice> generateInvoice(@PathVariable Long bookingId) {
        Invoice invoice = invoiceService.generateInvoice(bookingId);
        return ResponseEntity.ok(invoice);
    }

    /**
     * Get invoice data (JSON) - for frontend display
     * GET /api/invoices/{bookingId}
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<Invoice> getInvoice(@PathVariable Long bookingId) {
        Optional<Invoice> invoice = invoiceService.getInvoiceByBookingId(bookingId);
        if (invoice.isEmpty()) {
            // Generate invoice if not exists
            Invoice generated = invoiceService.generateInvoice(bookingId);
            return ResponseEntity.ok(generated);
        }
        return ResponseEntity.ok(invoice.get());
    }

    /**
     * Download invoice as PDF
     * GET /api/invoices/{bookingId}/pdf
     */
    @GetMapping("/{bookingId}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long bookingId) {
        byte[] pdf = pdfService.generatePdf(bookingId);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=gst-invoice-booking-" + bookingId + ".pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /**
     * Generate and immediately download PDF (for one-click download)
     * POST /api/invoices/{bookingId}/download
     */
    @PostMapping("/{bookingId}/download")
    public ResponseEntity<byte[]> generateAndDownload(@PathVariable Long bookingId) {
        // Generate invoice first (idempotent)
        invoiceService.generateInvoice(bookingId);

        // Then generate PDF
        byte[] pdf = pdfService.generatePdf(bookingId);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=gst-invoice-booking-" + bookingId + ".pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}

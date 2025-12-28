package com.billboarding.Controller.Payment;

import com.billboarding.Entity.User;
import com.billboarding.Services.Payment.GstInvoicePdfService;
import com.billboarding.Services.Payment.InvoiceService;
import com.billboarding.Services.Payment.PaymentInvoicePdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentInvoiceController {

    private final GstInvoicePdfService pdfService;

    /**
     * Download payment receipt PDF
     */

    @GetMapping("/invoice/gst/{bookingId}")
    public ResponseEntity<byte[]> downloadGstInvoice(
            @PathVariable Long bookingId) {

        byte[] pdf = pdfService.generateGstInvoice(bookingId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=GST-INVOICE-" + bookingId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

}

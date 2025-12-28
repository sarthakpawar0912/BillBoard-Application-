package com.billboarding.Services.Payment;

import com.billboarding.ENUM.PaymentStatus;
import com.billboarding.Entity.Payment.PaymentHistory;
import com.billboarding.Repository.Payment.PaymentHistoryRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class PaymentInvoicePdfService {

    private final PaymentHistoryRepository paymentRepo;

    /**
     * Generates PAYMENT RECEIPT PDF (not GST invoice yet)
     * Only for PAID bookings
     */
    public byte[] generatePaymentReceipt(Long bookingId) {

        PaymentHistory payment = paymentRepo.findByBooking_Id(bookingId)
                .stream()
                .filter(p -> p.getBooking().getPaymentStatus() == PaymentStatus.PAID)
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException("No successful payment found for this booking")
                );

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, out);

        doc.open();

        Font title = new Font(Font.HELVETICA, 20, Font.BOLD);
        Font bold = new Font(Font.HELVETICA, 12, Font.BOLD);

        doc.add(new Paragraph("PAYMENT RECEIPT", title));
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("Receipt Generated On: " + payment.getPaidAt()));
        doc.add(new Paragraph(" "));

        // Advertiser
        doc.add(new Paragraph("Advertiser Details", bold));
        doc.add(new Paragraph("Name: " + payment.getAdvertiser().getName()));
        doc.add(new Paragraph("Email: " + payment.getAdvertiser().getEmail()));

        doc.add(new Paragraph(" "));

        // Billboard
        doc.add(new Paragraph("Billboard Details", bold));
        doc.add(new Paragraph("Title: " + payment.getBooking().getBillboard().getTitle()));
        doc.add(new Paragraph("Location: " + payment.getBooking().getBillboard().getLocation()));

        doc.add(new Paragraph(" "));

        // Booking
        doc.add(new Paragraph("Booking Period", bold));
        doc.add(new Paragraph(
                payment.getBooking().getStartDate() +
                        " → " +
                        payment.getBooking().getEndDate()
        ));

        doc.add(new Paragraph(" "));

        // Payment
        doc.add(new Paragraph("Payment Details", bold));
        doc.add(new Paragraph("Amount Paid: ₹" + payment.getAmount()));
        doc.add(new Paragraph("Payment ID: " + payment.getRazorpayPaymentId()));
        doc.add(new Paragraph("Order ID: " + payment.getRazorpayOrderId()));

        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("This is a system-generated receipt.", bold));

        doc.close();
        return out.toByteArray();
    }
}

package com.billboarding.Services.Payment;

import com.billboarding.Entity.Payment.PaymentHistory;
import com.billboarding.Repository.Payment.PaymentHistoryRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final PaymentHistoryRepository paymentRepo;

    public byte[] generateInvoice(Long bookingId) {

        PaymentHistory pay = paymentRepo.findByBooking_Id(bookingId)
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Document doc = new Document();
        PdfWriter.getInstance(doc, out);

        doc.open();

        doc.add(new Paragraph("BILLBOARD INVOICE", new Font(Font.HELVETICA, 20, Font.BOLD)));
        doc.add(new Paragraph(" "));

        doc.add(new Paragraph("Advertiser: " + pay.getAdvertiser().getName()));
        doc.add(new Paragraph("Billboard: " + pay.getBooking().getBillboard().getTitle()));
        doc.add(new Paragraph("Booked Duration: " +
                pay.getBooking().getStartDate() + " → " + pay.getBooking().getEndDate()));
        doc.add(new Paragraph("Total Paid: ₹" + pay.getAmount()));
        doc.add(new Paragraph(" "));

        doc.add(new Paragraph("Payment ID: " + pay.getRazorpayPaymentId()));
        doc.add(new Paragraph("Order ID: " + pay.getRazorpayOrderId()));
        doc.add(new Paragraph("Paid At: " + pay.getPaidAt()));

        doc.close();
        return out.toByteArray();
    }


}
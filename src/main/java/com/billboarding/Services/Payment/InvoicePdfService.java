package com.billboarding.Services.Payment;

import com.billboarding.Entity.Payment.Invoice;
import com.billboarding.Repository.Payment.InvoiceRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class InvoicePdfService {

    private final InvoiceRepository invoiceRepo;

    public byte[] generatePdf(Long bookingId) {

        Invoice inv = invoiceRepo.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, out);

        doc.open();

        Font title = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font bold = new Font(Font.HELVETICA, 12, Font.BOLD);

        doc.add(new Paragraph("TAX INVOICE", title));
        doc.add(new Paragraph(" "));

        doc.add(new Paragraph("Invoice No: " + inv.getInvoiceNumber()));
        doc.add(new Paragraph("Invoice Date: " + inv.getInvoiceDate()));

        doc.add(new Paragraph("\nSeller Details", bold));
        doc.add(new Paragraph(inv.getSellerName()));
        doc.add(new Paragraph("GSTIN: " + inv.getSellerGstin()));
        doc.add(new Paragraph(inv.getSellerAddress()));

        doc.add(new Paragraph("\nBill To", bold));
        doc.add(new Paragraph(inv.getBuyerName()));
        doc.add(new Paragraph(inv.getBuyerEmail()));

        doc.add(new Paragraph("\nService Details", bold));
        doc.add(new Paragraph("Billboard: " + inv.getBillboardTitle()));
        doc.add(new Paragraph("Location: " + inv.getBillboardLocation()));
        doc.add(new Paragraph("Period: " +
                inv.getStartDate() + " to " + inv.getEndDate()));

        doc.add(new Paragraph("\nAmount Breakup", bold));
        doc.add(new Paragraph("Base Amount: ₹" + inv.getBaseAmount()));
        doc.add(new Paragraph("CGST (9%): ₹" + inv.getCgst()));
        doc.add(new Paragraph("SGST (9%): ₹" + inv.getSgst()));
        doc.add(new Paragraph("Total Amount: ₹" + inv.getTotalAmount()));

        doc.add(new Paragraph("\nThis is a system generated invoice."));

        doc.close();
        return out.toByteArray();
    }
}

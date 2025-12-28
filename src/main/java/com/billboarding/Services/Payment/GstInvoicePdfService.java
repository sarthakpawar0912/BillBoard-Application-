package com.billboarding.Services.Payment;

import com.billboarding.ENUM.PaymentStatus;
import com.billboarding.Entity.Payment.PaymentHistory;
import com.billboarding.Repository.Payment.PaymentHistoryRepository;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class GstInvoicePdfService {

    private final PaymentHistoryRepository paymentRepo;

    @Value("${company.name}")
    private String companyName;

    @Value("${company.gstin}")
    private String gstin;

    @Value("${company.address}")
    private String companyAddress;

    private static final double CGST = 0.09;
    private static final double SGST = 0.09;

    public byte[] generateGstInvoice(Long bookingId) {

        PaymentHistory pay = paymentRepo.findByBooking_Id(bookingId)
                .stream()
                .filter(p -> p.getBooking().getPaymentStatus() == PaymentStatus.PAID)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Paid booking not found"));

        double baseAmount = pay.getAmount();
        double cgst = baseAmount * CGST;
        double sgst = baseAmount * SGST;
        double total = baseAmount + cgst + sgst;

        long days = ChronoUnit.DAYS.between(
                pay.getBooking().getStartDate(),
                pay.getBooking().getEndDate()
        ) + 1;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(doc, out);
        doc.open();

        Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 11);

        // ================= HEADER =================
        Paragraph heading = new Paragraph("TAX INVOICE", title);
        heading.setAlignment(Element.ALIGN_CENTER);
        doc.add(heading);
        doc.add(new Paragraph(" "));

        // ================= SELLER & BUYER =================
        PdfPTable partyTable = new PdfPTable(2);
        partyTable.setWidthPercentage(100);
        partyTable.setSpacingBefore(10);
        partyTable.setWidths(new int[]{50, 50});

        PdfPCell seller = new PdfPCell();
        seller.addElement(new Paragraph("SELLER DETAILS", bold));
        seller.addElement(new Paragraph(companyName));
        seller.addElement(new Paragraph("GSTIN: " + gstin));
        seller.addElement(new Paragraph(companyAddress));
        seller.setBorder(Rectangle.BOX);

        PdfPCell buyer = new PdfPCell();
        buyer.addElement(new Paragraph("BUYER DETAILS", bold));
        buyer.addElement(new Paragraph(pay.getAdvertiser().getName()));
        buyer.addElement(new Paragraph(pay.getAdvertiser().getEmail()));
        buyer.setBorder(Rectangle.BOX);

        partyTable.addCell(seller);
        partyTable.addCell(buyer);
        doc.add(partyTable);

        // ================= SERVICE TABLE =================
        doc.add(new Paragraph(" "));
        PdfPTable serviceTable = new PdfPTable(6);
        serviceTable.setWidthPercentage(100);
        serviceTable.setWidths(new int[]{30, 15, 15, 10, 10, 20});

        addHeader(serviceTable, "Billboard");
        addHeader(serviceTable, "From");
        addHeader(serviceTable, "To");
        addHeader(serviceTable, "Days");
        addHeader(serviceTable, "Rate");
        addHeader(serviceTable, "Amount");

        serviceTable.addCell(pay.getBooking().getBillboard().getTitle());
        serviceTable.addCell(pay.getBooking().getStartDate().toString());
        serviceTable.addCell(pay.getBooking().getEndDate().toString());
        serviceTable.addCell(String.valueOf(days));
        serviceTable.addCell("₹" + (baseAmount / days));
        serviceTable.addCell("₹" + baseAmount);

        doc.add(serviceTable);

        // ================= TAX TABLE =================
        doc.add(new Paragraph(" "));
        PdfPTable taxTable = new PdfPTable(2);
        taxTable.setWidthPercentage(40);
        taxTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

        addRow(taxTable, "Sub Total", baseAmount);
        addRow(taxTable, "CGST 9%", cgst);
        addRow(taxTable, "SGST 9%", sgst);
        addRowBold(taxTable, "TOTAL", total);

        doc.add(taxTable);

        // ================= FOOTER =================
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("Payment ID: " + pay.getRazorpayPaymentId(), normal));
        doc.add(new Paragraph("Order ID: " + pay.getRazorpayOrderId(), normal));
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph(
                "This is a system generated GST invoice. No signature required.",
                bold
        ));

        doc.close();
        return out.toByteArray();
    }

    private void addHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text));
        cell.setBackgroundColor(Color.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addRow(PdfPTable table, String label, double value) {
        table.addCell(label);
        table.addCell("₹" + String.format("%.2f", value));
    }

    private void addRowBold(PdfPTable table, String label, double value) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
        PdfPCell c2 = new PdfPCell(new Phrase("₹" + String.format("%.2f", value),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
        table.addCell(c1);
        table.addCell(c2);
    }
}

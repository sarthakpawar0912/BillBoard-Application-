package com.billboarding.Services.Payment;

import com.billboarding.Entity.Payment.Invoice;
import com.billboarding.Repository.Payment.InvoiceRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class InvoicePdfService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceService invoiceService;

    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##,##0.00");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    /**
     * Generate professional GST Invoice PDF
     */
    public byte[] generatePdf(Long bookingId) {

        // Generate invoice if not exists (idempotent)
        Invoice inv = invoiceService.generateInvoice(bookingId);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(doc, out);
        doc.open();

        // Fonts
        Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD, new Color(51, 51, 51));
        Font headerFont = new Font(Font.HELVETICA, 11, Font.BOLD, new Color(51, 51, 51));
        Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(68, 68, 68));
        Font smallFont = new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(100, 100, 100));
        Font boldFont = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(51, 51, 51));
        Font totalFont = new Font(Font.HELVETICA, 12, Font.BOLD, new Color(51, 51, 51));

        // ================= HEADER =================
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        try {
            headerTable.setWidths(new float[]{60, 40});
        } catch (Exception ignored) {}

        // Title cell
        PdfPCell titleCell = new PdfPCell();
        titleCell.setBorder(Rectangle.NO_BORDER);
        Paragraph titlePara = new Paragraph("TAX INVOICE", titleFont);
        titleCell.addElement(titlePara);
        headerTable.addCell(titleCell);

        // Invoice details cell (right aligned)
        PdfPCell invoiceDetailsCell = new PdfPCell();
        invoiceDetailsCell.setBorder(Rectangle.NO_BORDER);
        invoiceDetailsCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Paragraph invNoPara = new Paragraph("Invoice No: " + inv.getInvoiceNumber(), boldFont);
        invNoPara.setAlignment(Element.ALIGN_RIGHT);
        invoiceDetailsCell.addElement(invNoPara);

        Paragraph invDatePara = new Paragraph("Date: " + inv.getInvoiceDate().format(DATE_FORMAT), normalFont);
        invDatePara.setAlignment(Element.ALIGN_RIGHT);
        invoiceDetailsCell.addElement(invDatePara);

        headerTable.addCell(invoiceDetailsCell);
        doc.add(headerTable);
        doc.add(new Paragraph(" "));

        // ================= SELLER & BUYER =================
        PdfPTable partyTable = new PdfPTable(2);
        partyTable.setWidthPercentage(100);
        partyTable.setSpacingBefore(10);

        // Seller details
        PdfPCell sellerCell = createPartyCell("SELLER DETAILS",
                inv.getSellerName(),
                "GSTIN: " + inv.getSellerGstin(),
                inv.getSellerAddress(),
                inv.getSellerState() + " (Code: " + inv.getSellerStateCode() + ")",
                headerFont, normalFont);
        partyTable.addCell(sellerCell);

        // Buyer details
        String buyerGstLine = inv.getBuyerGstin() != null && !inv.getBuyerGstin().isBlank()
                ? "GSTIN: " + inv.getBuyerGstin()
                : "GSTIN: N/A (Unregistered)";
        PdfPCell buyerCell = createPartyCell("BUYER DETAILS",
                inv.getBuyerName(),
                buyerGstLine,
                inv.getBuyerEmail() + (inv.getBuyerPhone() != null ? " | " + inv.getBuyerPhone() : ""),
                inv.getBuyerState() != null ? inv.getBuyerState() + " (Code: " + inv.getBuyerStateCode() + ")" : "",
                headerFont, normalFont);
        partyTable.addCell(buyerCell);

        doc.add(partyTable);
        doc.add(new Paragraph(" "));

        // ================= SERVICE DETAILS =================
        PdfPTable serviceTable = new PdfPTable(6);
        serviceTable.setWidthPercentage(100);
        serviceTable.setSpacingBefore(10);
        try {
            serviceTable.setWidths(new float[]{30, 12, 12, 10, 18, 18});
        } catch (Exception ignored) {}

        // Header row
        addTableHeader(serviceTable, new String[]{"Description", "SAC", "From", "To", "Days", "Amount"}, headerFont);

        // Service row
        String description = "Billboard Advertising Service\n" + inv.getBillboardTitle() + "\n@ " + inv.getBillboardLocation();
        addTableCell(serviceTable, description, normalFont, Element.ALIGN_LEFT);
        addTableCell(serviceTable, inv.getSacCode() != null ? inv.getSacCode() : "998365", normalFont, Element.ALIGN_CENTER);
        addTableCell(serviceTable, inv.getStartDate().format(DATE_FORMAT), normalFont, Element.ALIGN_CENTER);
        addTableCell(serviceTable, inv.getEndDate().format(DATE_FORMAT), normalFont, Element.ALIGN_CENTER);
        addTableCell(serviceTable, String.valueOf(inv.getTotalDays()), normalFont, Element.ALIGN_CENTER);
        addTableCell(serviceTable, formatCurrency(inv.getBaseAmount()), normalFont, Element.ALIGN_RIGHT);

        doc.add(serviceTable);

        // ================= AMOUNT BREAKDOWN =================
        doc.add(new Paragraph(" "));
        PdfPTable amountTable = new PdfPTable(2);
        amountTable.setWidthPercentage(50);
        amountTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        try {
            amountTable.setWidths(new float[]{60, 40});
        } catch (Exception ignored) {}

        // Billboard Rental
        addAmountRow(amountTable, "Billboard Rental", formatCurrency(inv.getBaseAmount()), normalFont);

        // Platform Fee (Commission)
        String commissionLabel = "Platform Service Fee";
        if (inv.getCommissionPercent() != null && inv.getCommissionPercent() > 0) {
            commissionLabel += " (" + CURRENCY_FORMAT.format(inv.getCommissionPercent()) + "%)";
        }
        addAmountRow(amountTable, commissionLabel, formatCurrency(inv.getCommissionAmount()), normalFont);

        // Taxable Value
        addAmountRow(amountTable, "Taxable Value", formatCurrency(inv.getTaxableValue()), boldFont);

        // GST breakdown
        if (inv.getCgst() != null && inv.getCgst() > 0) {
            String cgstLabel = "CGST @ " + (inv.getCgstPercent() != null ? CURRENCY_FORMAT.format(inv.getCgstPercent()) + "%" : "9%");
            addAmountRow(amountTable, cgstLabel, formatCurrency(inv.getCgst()), normalFont);
        }
        if (inv.getSgst() != null && inv.getSgst() > 0) {
            String sgstLabel = "SGST @ " + (inv.getSgstPercent() != null ? CURRENCY_FORMAT.format(inv.getSgstPercent()) + "%" : "9%");
            addAmountRow(amountTable, sgstLabel, formatCurrency(inv.getSgst()), normalFont);
        }
        if (inv.getIgst() != null && inv.getIgst() > 0) {
            String igstLabel = "IGST @ " + (inv.getIgstPercent() != null ? CURRENCY_FORMAT.format(inv.getIgstPercent()) + "%" : "18%");
            addAmountRow(amountTable, igstLabel, formatCurrency(inv.getIgst()), normalFont);
        }

        // Total GST
        addAmountRow(amountTable, "Total GST", formatCurrency(inv.getTotalGst()), boldFont);

        // Separator
        PdfPCell sepCell = new PdfPCell(new Phrase(""));
        sepCell.setColspan(2);
        sepCell.setBorder(Rectangle.BOTTOM);
        sepCell.setBorderColor(new Color(200, 200, 200));
        sepCell.setFixedHeight(5);
        amountTable.addCell(sepCell);

        // Grand Total
        PdfPCell totalLabelCell = new PdfPCell(new Phrase("GRAND TOTAL", totalFont));
        totalLabelCell.setBorder(Rectangle.NO_BORDER);
        totalLabelCell.setPadding(5);
        totalLabelCell.setBackgroundColor(new Color(240, 240, 240));
        amountTable.addCell(totalLabelCell);

        PdfPCell totalValueCell = new PdfPCell(new Phrase(formatCurrency(inv.getTotalAmount()), totalFont));
        totalValueCell.setBorder(Rectangle.NO_BORDER);
        totalValueCell.setPadding(5);
        totalValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalValueCell.setBackgroundColor(new Color(240, 240, 240));
        amountTable.addCell(totalValueCell);

        doc.add(amountTable);

        // ================= PAYMENT INFO =================
        if (inv.getRazorpayPaymentId() != null || inv.getRazorpayOrderId() != null) {
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("PAYMENT DETAILS", headerFont));
            doc.add(new Paragraph(" "));

            PdfPTable paymentTable = new PdfPTable(2);
            paymentTable.setWidthPercentage(60);
            paymentTable.setHorizontalAlignment(Element.ALIGN_LEFT);

            if (inv.getRazorpayPaymentId() != null) {
                addPaymentRow(paymentTable, "Payment ID:", inv.getRazorpayPaymentId(), normalFont);
            }
            if (inv.getRazorpayOrderId() != null) {
                addPaymentRow(paymentTable, "Order ID:", inv.getRazorpayOrderId(), normalFont);
            }
            if (inv.getPaymentDate() != null) {
                addPaymentRow(paymentTable, "Payment Date:", inv.getPaymentDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")), normalFont);
            }
            addPaymentRow(paymentTable, "Payment Status:", "PAID", boldFont);

            doc.add(paymentTable);
        }

        // ================= FOOTER =================
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph(" "));

        Paragraph footerNote = new Paragraph(
                "This is a computer-generated GST invoice and does not require a physical signature.",
                smallFont
        );
        footerNote.setAlignment(Element.ALIGN_CENTER);
        doc.add(footerNote);

        Paragraph thankYou = new Paragraph("Thank you for your business!", smallFont);
        thankYou.setAlignment(Element.ALIGN_CENTER);
        doc.add(thankYou);

        doc.close();
        return out.toByteArray();
    }

    // ================= HELPER METHODS =================

    private PdfPCell createPartyCell(String title, String name, String gstin, String contact, String state, Font headerFont, Font normalFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(new Color(200, 200, 200));
        cell.setPadding(10);

        Paragraph titlePara = new Paragraph(title, headerFont);
        cell.addElement(titlePara);

        Paragraph namePara = new Paragraph(name, new Font(Font.HELVETICA, 11, Font.BOLD));
        cell.addElement(namePara);

        cell.addElement(new Paragraph(gstin, normalFont));
        cell.addElement(new Paragraph(contact, normalFont));
        if (state != null && !state.isBlank()) {
            cell.addElement(new Paragraph(state, normalFont));
        }

        return cell;
    }

    private void addTableHeader(PdfPTable table, String[] headers, Font font) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, font));
            cell.setBackgroundColor(new Color(240, 240, 240));
            cell.setPadding(8);
            cell.setBorderColor(new Color(200, 200, 200));
            table.addCell(cell);
        }
    }

    private void addTableCell(PdfPTable table, String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        cell.setHorizontalAlignment(alignment);
        cell.setBorderColor(new Color(200, 200, 200));
        table.addCell(cell);
    }

    private void addAmountRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(5);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private void addPaymentRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(3);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(3);
        table.addCell(valueCell);
    }

    private String formatCurrency(Double amount) {
        if (amount == null) return "0.00";
        return "Rs. " + CURRENCY_FORMAT.format(amount);
    }
}

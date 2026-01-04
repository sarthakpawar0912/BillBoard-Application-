package com.billboarding.Services.Owner;

import com.billboarding.DTO.OWNER.BillboardRevenueDTO;
import com.billboarding.DTO.OWNER.MonthlyRevenueDTO;
import com.billboarding.DTO.OWNER.OwnerRevenueDashboardResponse;
import com.billboarding.Entity.User;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class OwnerRevenueReportService {

    private final OwnerRevenueService revenueService;
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.of("en", "IN"));
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public byte[] exportRevenueCsv(User owner, Long billboardId, LocalDate start, LocalDate end) {
        OwnerRevenueDashboardResponse data =
                revenueService.getRevenueDashboard(owner, billboardId, start, end);

        StringBuilder sb = new StringBuilder();

        // Header section
        sb.append("BILLBOARD REVENUE REPORT\n");
        sb.append("========================\n\n");
        sb.append("Owner Name,").append(escapeCSV(owner.getName())).append("\n");
        sb.append("Email,").append(escapeCSV(owner.getEmail())).append("\n");
        sb.append("Generated On,").append(LocalDate.now().format(DATE_FORMATTER)).append("\n");

        if (start != null || end != null) {
            sb.append("Date Range,");
            if (start != null && end != null) {
                sb.append(start.format(DATE_FORMATTER)).append(" to ").append(end.format(DATE_FORMATTER));
            } else if (start != null) {
                sb.append("From ").append(start.format(DATE_FORMATTER));
            } else {
                sb.append("Until ").append(end.format(DATE_FORMATTER));
            }
            sb.append("\n");
        }
        sb.append("\n");

        // Summary statistics
        sb.append("SUMMARY\n");
        sb.append("-------\n");
        sb.append("Total Earnings,").append(formatAmount(data.getTotalEarnings())).append("\n");
        sb.append("Total Billboards,").append(data.getTotalBillboards()).append("\n");
        sb.append("Total Bookings,").append(data.getTotalBookings()).append("\n");
        sb.append("Pending Requests,").append(data.getPendingRequests()).append("\n");
        sb.append("\n");

        // Billboard details
        sb.append("BILLBOARD DETAILS\n");
        sb.append("-----------------\n");
        sb.append("Billboard ID,Title,Location,Type,Bookings,Revenue,Images\n");

        for (BillboardRevenueDTO b : data.getBillboards()) {
            sb.append(b.getBillboardId()).append(",")
              .append(escapeCSV(b.getTitle() != null ? b.getTitle() : "N/A")).append(",")
              .append(escapeCSV(b.getLocation() != null ? b.getLocation() : "N/A")).append(",")
              .append(b.getType()).append(",")
              .append(b.getTotalBookings()).append(",")
              .append(formatAmount(b.getTotalRevenue())).append(",")
              .append(b.getImageCount()).append("\n");
        }

        // Monthly breakdown (if available)
        if (data.getMonthlyRevenue() != null && !data.getMonthlyRevenue().isEmpty()) {
            sb.append("\nMONTHLY REVENUE\n");
            sb.append("---------------\n");
            sb.append("Month,Year,Revenue\n");
            for (MonthlyRevenueDTO m : data.getMonthlyRevenue()) {
                sb.append(getMonthName(m.getMonth())).append(",")
                  .append(m.getYear()).append(",")
                  .append(formatAmount(m.getTotalRevenue())).append("\n");
            }
        }

        sb.append("\n--- End of Report ---\n");

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportRevenuePdf(User owner, Long billboardId, LocalDate start, LocalDate end) {
        OwnerRevenueDashboardResponse data =
                revenueService.getRevenueDashboard(owner, billboardId, start, end);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Document doc = new Document(PageSize.A4, 40, 40, 50, 50);
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            doc.open();

            // Fonts
            Font titleFont = new Font(Font.HELVETICA, 22, Font.BOLD, new Color(22, 163, 74));
            Font subtitleFont = new Font(Font.HELVETICA, 11, Font.NORMAL, new Color(100, 116, 139));
            Font sectionFont = new Font(Font.HELVETICA, 14, Font.BOLD, new Color(30, 41, 59));
            Font labelFont = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(100, 116, 139));
            Font valueFont = new Font(Font.HELVETICA, 12, Font.BOLD, new Color(30, 41, 59));
            Font tableHeaderFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
            Font tableCellFont = new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(30, 41, 59));
            Font amountFont = new Font(Font.HELVETICA, 9, Font.BOLD, new Color(22, 163, 74));

            // Header
            Paragraph title = new Paragraph("Billboard Revenue Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);

            // Subtitle with owner info and date
            Paragraph subtitle = new Paragraph();
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.add(new Chunk("Owner: " + owner.getName() + " | ", subtitleFont));
            subtitle.add(new Chunk("Generated: " + LocalDate.now().format(DATE_FORMATTER), subtitleFont));
            subtitle.setSpacingAfter(5);
            doc.add(subtitle);

            // Date range if specified
            if (start != null || end != null) {
                Paragraph dateRange = new Paragraph();
                dateRange.setAlignment(Element.ALIGN_CENTER);
                String rangeText = "Period: ";
                if (start != null && end != null) {
                    rangeText += start.format(DATE_FORMATTER) + " to " + end.format(DATE_FORMATTER);
                } else if (start != null) {
                    rangeText += "From " + start.format(DATE_FORMATTER);
                } else {
                    rangeText += "Until " + end.format(DATE_FORMATTER);
                }
                dateRange.add(new Chunk(rangeText, subtitleFont));
                dateRange.setSpacingAfter(20);
                doc.add(dateRange);
            } else {
                doc.add(new Paragraph("\n"));
            }

            // Summary Stats Table
            PdfPTable summaryTable = new PdfPTable(4);
            summaryTable.setWidthPercentage(100);
            summaryTable.setSpacingBefore(10);
            summaryTable.setSpacingAfter(20);

            addSummaryCell(summaryTable, "Total Earnings", formatCurrency(data.getTotalEarnings()), new Color(220, 252, 231), labelFont, valueFont);
            addSummaryCell(summaryTable, "Billboards", String.valueOf(data.getTotalBillboards()), new Color(237, 233, 254), labelFont, valueFont);
            addSummaryCell(summaryTable, "Total Bookings", String.valueOf(data.getTotalBookings()), new Color(254, 243, 199), labelFont, valueFont);
            addSummaryCell(summaryTable, "Pending", String.valueOf(data.getPendingRequests()), new Color(254, 226, 226), labelFont, valueFont);

            doc.add(summaryTable);

            // Billboard Performance Section
            Paragraph sectionTitle = new Paragraph("Billboard Performance", sectionFont);
            sectionTitle.setSpacingBefore(10);
            sectionTitle.setSpacingAfter(10);
            doc.add(sectionTitle);

            // Billboard Table
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 2.5f, 2.5f, 1.5f, 1.2f, 1.5f});

            Color headerBg = new Color(22, 163, 74);
            addTableHeader(table, "ID", headerBg, tableHeaderFont);
            addTableHeader(table, "Title", headerBg, tableHeaderFont);
            addTableHeader(table, "Location", headerBg, tableHeaderFont);
            addTableHeader(table, "Type", headerBg, tableHeaderFont);
            addTableHeader(table, "Bookings", headerBg, tableHeaderFont);
            addTableHeader(table, "Revenue", headerBg, tableHeaderFont);

            boolean alternate = false;
            for (BillboardRevenueDTO b : data.getBillboards()) {
                Color rowBg = alternate ? new Color(248, 250, 252) : Color.WHITE;

                addTableCell(table, String.valueOf(b.getBillboardId()), rowBg, tableCellFont, Element.ALIGN_CENTER);
                addTableCell(table, b.getTitle() != null ? b.getTitle() : "N/A", rowBg, tableCellFont, Element.ALIGN_LEFT);
                addTableCell(table, b.getLocation() != null ? truncate(b.getLocation(), 30) : "N/A", rowBg, tableCellFont, Element.ALIGN_LEFT);
                addTableCell(table, b.getType(), rowBg, tableCellFont, Element.ALIGN_CENTER);
                addTableCell(table, String.valueOf(b.getTotalBookings()), rowBg, tableCellFont, Element.ALIGN_CENTER);
                addTableCell(table, formatCurrency(b.getTotalRevenue()), rowBg, amountFont, Element.ALIGN_RIGHT);

                alternate = !alternate;
            }

            doc.add(table);

            // Footer
            Paragraph footer = new Paragraph();
            footer.setSpacingBefore(30);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.add(new Chunk("--- End of Report ---", subtitleFont));
            doc.add(footer);

            doc.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }
    }

    private void addSummaryCell(PdfPTable table, String label, String value, Color bgColor, Font labelFont, Font valueFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(bgColor);
        cell.setPadding(12);
        cell.setBorder(Rectangle.NO_BORDER);

        Paragraph p = new Paragraph();
        p.add(new Chunk(label + "\n", labelFont));
        p.add(new Chunk(value, valueFont));
        cell.addElement(p);

        table.addCell(cell);
    }

    private void addTableHeader(PdfPTable table, String text, Color bgColor, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(8);
        cell.setBorderWidth(0);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text, Color bgColor, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);
        cell.setBorderColor(new Color(226, 232, 240));
        cell.setBorderWidth(0.5f);
        table.addCell(cell);
    }

    private String formatCurrency(double amount) {
        return "₹" + String.format("%,.0f", amount);
    }

    private String formatAmount(double amount) {
        return String.format("%.2f", amount);
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    private String getMonthName(int month) {
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        if (month >= 1 && month <= 12) return months[month - 1];
        return String.valueOf(month);
    }
}

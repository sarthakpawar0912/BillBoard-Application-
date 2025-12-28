package com.billboarding.Services.Owner;

import com.billboarding.DTO.OWNER.BillboardRevenueDTO;
import com.billboarding.DTO.OWNER.OwnerRevenueDashboardResponse;
import com.billboarding.Entity.User;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class OwnerRevenueReportService {

    private final OwnerRevenueService revenueService;

    public byte[] exportRevenueCsv(User owner, Long billboardId, LocalDate start, LocalDate end) {

        OwnerRevenueDashboardResponse data =
                revenueService.getRevenueDashboard(owner, billboardId, start, end);

        StringBuilder sb = new StringBuilder("Billboard ID,Title,Location,Bookings,Revenue\n");

        for (BillboardRevenueDTO b : data.getBillboards()) {
            sb.append(b.getBillboardId()).append(",")
                    .append(b.getTitle()).append(",")
                    .append(b.getLocation()).append(",")
                    .append(b.getTotalBookings()).append(",")
                    .append(b.getTotalRevenue()).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportRevenuePdf(User owner, Long billboardId, LocalDate start, LocalDate end) {

        OwnerRevenueDashboardResponse data =
                revenueService.getRevenueDashboard(owner, billboardId, start, end);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);

            doc.add(new Paragraph("Owner Revenue Report", titleFont));
            doc.add(new Paragraph("Total Earnings: ₹" + data.getTotalEarnings()));

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);

            addHeader(table, "ID");
            addHeader(table, "Title");
            addHeader(table, "Location");
            addHeader(table, "Bookings");
            addHeader(table, "Revenue");

            for (BillboardRevenueDTO b : data.getBillboards()) {
                table.addCell(b.getBillboardId().toString());
                table.addCell(b.getTitle());
                table.addCell(b.getLocation());
                table.addCell(String.valueOf(b.getTotalBookings()));
                table.addCell("₹" + b.getTotalRevenue());
            }

            doc.add(table);
            doc.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    private void addHeader(PdfPTable table, String title) {
        PdfPCell cell = new PdfPCell(new Phrase(title));
        cell.setBackgroundColor(Color.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }
}

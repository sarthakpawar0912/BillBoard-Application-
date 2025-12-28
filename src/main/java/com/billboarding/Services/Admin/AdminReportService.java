package com.billboarding.Services.Admin;


import com.billboarding.ENUM.BookingStatus;
import com.billboarding.ENUM.PaymentStatus;
import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Entity.User;
import com.billboarding.Repository.Booking.BookingRepository;
import com.billboarding.Repository.UserRepository;
import com.billboarding.Entity.OWNER.Billboard;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    // ======== CSV HELPERS ========

    private String escapeCsv(String value) {
        if (value == null) return "";
        String v = value.replace("\"", "\"\"");
        return "\"" + v + "\"";
    }

    // ======== USERS CSV / PDF ========

    public byte[] exportUsersCsv() {
        List<User> users = userRepository.findAll();

        StringBuilder sb = new StringBuilder();
        sb.append("Id,Name,Email,Phone,Role,KycStatus,Blocked,CreatedAt\n");

        for (User u : users) {
            sb.append(u.getId()).append(",");
            sb.append(escapeCsv(u.getName())).append(",");
            sb.append(escapeCsv(u.getEmail())).append(",");
            sb.append(escapeCsv(u.getPhone())).append(",");
            sb.append(u.getRole() != null ? u.getRole().name() : "").append(",");
            sb.append(u.getKycStatus() != null ? u.getKycStatus().name() : "").append(",");
            sb.append(u.isBlocked()).append(",");
            sb.append(u.getCreatedAt() != null ? u.getCreatedAt().toString() : "");
            sb.append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportUsersPdf() {
        List<User> users = userRepository.findAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Document document = new Document(PageSize.A4.rotate());
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Title
            document.add(new Paragraph("Users Report",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            document.add(new Paragraph(" "));

            // Table with 8 columns
            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);

            table.addCell("Id");
            table.addCell("Name");
            table.addCell("Email");
            table.addCell("Phone");
            table.addCell("Role");
            table.addCell("KYC Status");
            table.addCell("Blocked");
            table.addCell("Created At");

            for (User u : users) {
                table.addCell(String.valueOf(u.getId()));
                table.addCell(u.getName() != null ? u.getName() : "");
                table.addCell(u.getEmail() != null ? u.getEmail() : "");
                table.addCell(u.getPhone() != null ? u.getPhone() : "");
                table.addCell(u.getRole() != null ? u.getRole().name() : "");
                table.addCell(u.getKycStatus() != null ? u.getKycStatus().name() : "");
                table.addCell(String.valueOf(u.isBlocked()));
                table.addCell(u.getCreatedAt() != null ? u.getCreatedAt().toString() : "");
            }

            document.add(table);
        } catch (Exception e) {
            throw new RuntimeException("Error generating Users PDF report", e);
        } finally {
            document.close();
        }

        return out.toByteArray();
    }

    // ======== BOOKINGS / REVENUE CSV / PDF ========

    public byte[] exportBookingsCsv() {
        List<Booking> bookings = bookingRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("Id,AdvertiserEmail,BillboardTitle,StartDate,EndDate,TotalPrice,Status,PaymentStatus,CreatedAt\n");

        for (Booking b : bookings) {
            User adv = b.getAdvertiser();
            Billboard board = b.getBillboard();

            sb.append(b.getId()).append(",");
            sb.append(escapeCsv(adv != null ? adv.getEmail() : "")).append(",");
            sb.append(escapeCsv(board != null ? board.getTitle() : "")).append(",");
            sb.append(b.getStartDate() != null ? b.getStartDate().toString() : "").append(",");
            sb.append(b.getEndDate() != null ? b.getEndDate().toString() : "").append(",");
            sb.append(b.getTotalPrice() != null ? b.getTotalPrice() : 0.0).append(",");
            sb.append(b.getStatus() != null ? b.getStatus().name() : "").append(",");
            sb.append(b.getPaymentStatus() != null ? b.getPaymentStatus().name() : "").append(",");
            sb.append(b.getCreatedAt() != null ? b.getCreatedAt().toString() : "");
            sb.append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportBookingsPdf() {
        List<Booking> bookings = bookingRepository.findAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Document document = new Document(PageSize.A4.rotate());
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph("Bookings Report",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);

            table.addCell("Id");
            table.addCell("Advertiser Email");
            table.addCell("Billboard Title");
            table.addCell("Start Date");
            table.addCell("End Date");
            table.addCell("Total Price");
            table.addCell("Status");
            table.addCell("Payment Status");
            table.addCell("Created At");

            for (Booking b : bookings) {
                User adv = b.getAdvertiser();
                Billboard board = b.getBillboard();

                table.addCell(String.valueOf(b.getId()));
                table.addCell(adv != null ? adv.getEmail() : "");
                table.addCell(board != null ? board.getTitle() : "");
                table.addCell(b.getStartDate() != null ? b.getStartDate().toString() : "");
                table.addCell(b.getEndDate() != null ? b.getEndDate().toString() : "");
                table.addCell(b.getTotalPrice() != null ? b.getTotalPrice().toString() : "0");
                table.addCell(b.getStatus() != null ? b.getStatus().name() : "");
                table.addCell(b.getPaymentStatus() != null ? b.getPaymentStatus().name() : "");
                table.addCell(b.getCreatedAt() != null ? b.getCreatedAt().toString() : "");
            }

            document.add(table);
        } catch (Exception e) {
            throw new RuntimeException("Error generating Bookings PDF report", e);
        } finally {
            document.close();
        }

        return out.toByteArray();
    }

    /**
     * Revenue report (only APPROVED + PAID bookings).
     * CSV with one row per booking + last row "TOTAL".
     */
    public byte[] exportRevenueCsv() {
        List<Booking> bookings = bookingRepository.findAll();

        StringBuilder sb = new StringBuilder();
        sb.append("Id,AdvertiserEmail,BillboardTitle,StartDate,EndDate,TotalPrice,Status,PaymentStatus\n");

        double totalRevenue = 0.0;

        for (Booking b : bookings) {
            if (b.getStatus() == BookingStatus.APPROVED &&
                    b.getPaymentStatus() == PaymentStatus.PAID) {

                User adv = b.getAdvertiser();
                Billboard board = b.getBillboard();

                sb.append(b.getId()).append(",");
                sb.append(escapeCsv(adv != null ? adv.getEmail() : "")).append(",");
                sb.append(escapeCsv(board != null ? board.getTitle() : "")).append(",");
                sb.append(b.getStartDate() != null ? b.getStartDate().toString() : "").append(",");
                sb.append(b.getEndDate() != null ? b.getEndDate().toString() : "").append(",");
                sb.append(b.getTotalPrice() != null ? b.getTotalPrice() : 0.0).append(",");
                sb.append(b.getStatus().name()).append(",");
                sb.append(b.getPaymentStatus().name()).append("\n");

                if (b.getTotalPrice() != null) {
                    totalRevenue += b.getTotalPrice();
                }
            }
        }

        sb.append("\nTOTAL_REVENUE,").append(totalRevenue).append("\n");

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportRevenuePdf() {
        List<Booking> bookings = bookingRepository.findAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        double totalRevenue = 0.0;

        Document document = new Document(PageSize.A4.rotate());
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph("Revenue Report (Approved + Paid Bookings)",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);

            table.addCell("Id");
            table.addCell("Advertiser Email");
            table.addCell("Billboard Title");
            table.addCell("Start Date");
            table.addCell("End Date");
            table.addCell("Total Price");
            table.addCell("Status");
            table.addCell("Payment Status");

            for (Booking b : bookings) {
                if (b.getStatus() == BookingStatus.APPROVED &&
                        b.getPaymentStatus() == PaymentStatus.PAID) {

                    User adv = b.getAdvertiser();
                    Billboard board = b.getBillboard();

                    table.addCell(String.valueOf(b.getId()));
                    table.addCell(adv != null ? adv.getEmail() : "");
                    table.addCell(board != null ? board.getTitle() : "");
                    table.addCell(b.getStartDate() != null ? b.getStartDate().toString() : "");
                    table.addCell(b.getEndDate() != null ? b.getEndDate().toString() : "");
                    table.addCell(b.getTotalPrice() != null ? b.getTotalPrice().toString() : "0");
                    table.addCell(b.getStatus().name());
                    table.addCell(b.getPaymentStatus().name());

                    if (b.getTotalPrice() != null) {
                        totalRevenue += b.getTotalPrice();
                    }
                }
            }

            document.add(table);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Total Revenue: " + totalRevenue,
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));

        } catch (Exception e) {
            throw new RuntimeException("Error generating Revenue PDF report", e);
        } finally {
            document.close();
        }

        return out.toByteArray();
    }
}
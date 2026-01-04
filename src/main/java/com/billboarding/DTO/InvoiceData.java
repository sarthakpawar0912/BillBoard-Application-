package com.billboarding.DTO;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class InvoiceData {
    private String invoiceNo;
    private String advertiserName;
    private String billboardTitle;
    private LocalDate startDate;
    private LocalDate endDate;

    private double baseAmount;
    private double gstPercent;
    private double gstAmount;
    private double totalAmount;
}

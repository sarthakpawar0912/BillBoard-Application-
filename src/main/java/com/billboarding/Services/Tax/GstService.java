package com.billboarding.Services.Tax;

import org.springframework.stereotype.Service;

@Service
public class GstService {

    private static final double GST_PERCENT = 18.0;

    public double getGstPercent() {
        return GST_PERCENT;
    }

    public double calculateGst(double baseAmount) {
        return baseAmount * GST_PERCENT / 100;
    }

    public double calculateFinalAmount(double baseAmount) {
        return baseAmount + calculateGst(baseAmount);
    }
}

package com.billboarding.ENUM;

public enum BookingStatus {
    PENDING,      // Advertiser requested, waiting for owner/admin decision
    APPROVED,     // Booking approved (can proceed to payment / running)
    REJECTED,     // Booking rejected by owner/admin
    CANCELLED ,    // Cancelled by advertiser or system
    CANCELLED_NO_REFUND
}


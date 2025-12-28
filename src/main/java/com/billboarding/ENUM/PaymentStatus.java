package com.billboarding.ENUM;

public enum PaymentStatus {
    NOT_PAID,   // Just created, no payment done
    PENDING,    // Payment initiated but not completed
    PAID,       // Fully paid
    FAILED,     // Payment attempt failed
    REFUNDED    // Money refunded
}

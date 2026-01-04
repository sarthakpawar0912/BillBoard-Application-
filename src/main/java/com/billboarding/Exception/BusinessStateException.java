package com.billboarding.Exception;

/**
 * Exception thrown when a business rule violation occurs.
 * Results in HTTP 409 Conflict response.
 *
 * Examples:
 * - Trying to pay for an already paid booking
 * - Trying to approve a non-pending booking
 * - Trying to cancel a completed booking
 * - Duplicate payment attempts
 */
public class BusinessStateException extends RuntimeException {

    public BusinessStateException(String message) {
        super(message);
    }

    public static BusinessStateException alreadyPaid(Long bookingId) {
        return new BusinessStateException("Booking #" + bookingId + " has already been paid");
    }

    public static BusinessStateException invalidStateTransition(String from, String to) {
        return new BusinessStateException("Cannot transition from " + from + " to " + to);
    }

    public static BusinessStateException duplicateOperation(String operation) {
        return new BusinessStateException(operation + " has already been performed");
    }
}

package com.billboarding.Services.Admin;

import com.billboarding.DTO.Cancellation.AdminCancellationStats;
import com.billboarding.ENUM.BookingStatus;
import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Repository.Booking.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminAnalyticsService {

    private final BookingRepository bookingRepo;

    public AdminCancellationStats getCancellationStats() {

        List<Booking> cancelled =
                bookingRepo.findByStatusIn(
                        List.of(
                                BookingStatus.CANCELLED,
                                BookingStatus.CANCELLED_NO_REFUND
                        )
                );

        long noRefund = cancelled.stream()
                .filter(b -> b.getStatus()
                        == BookingStatus.CANCELLED_NO_REFUND)
                .count();

        double retained = cancelled.stream()
                .filter(b -> b.getStatus()
                        == BookingStatus.CANCELLED_NO_REFUND)
                .mapToDouble(Booking::getTotalPrice)
                .sum();

        return new AdminCancellationStats(
                cancelled.size(),
                noRefund,
                cancelled.size() - noRefund,
                retained
        );
    }
}

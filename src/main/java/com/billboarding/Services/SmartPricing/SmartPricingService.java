package com.billboarding.Services.SmartPricing;

import com.billboarding.ENUM.BookingStatus;
import com.billboarding.Entity.OWNER.Billboard;
import com.billboarding.Repository.Booking.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
@Service
@RequiredArgsConstructor
public class SmartPricingService {

    private final BookingRepository bookingRepo;

    public double calculateBasePrice(
            Billboard billboard,
            LocalDate start,
            LocalDate end
    ) {
        long days = ChronoUnit.DAYS.between(start, end) + 1;
        double base = billboard.getPricePerDay() * days;

        long demand = bookingRepo
                .countByBillboardAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        billboard,
                        BookingStatus.APPROVED,
                        end,
                        start
                );

        // Demand surge
        if (demand > 5) base *= 1.3;

        // Weekend surge
        if (start.getDayOfWeek().getValue() >= 6)
            base *= 1.2;

        return Math.round(base);
    }
}

package com.billboarding.Services.Owner;

import com.billboarding.DTO.OWNER.OwnerCalendarDayResponse;
import com.billboarding.ENUM.BookingStatus;
import com.billboarding.ENUM.PaymentStatus;
import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Entity.OWNER.Billboard;
import com.billboarding.Repository.Booking.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
@RequiredArgsConstructor
public class OwnerCalendarService {

    private final BookingRepository bookingRepo;

    public List<OwnerCalendarDayResponse> getCalendar(
            Billboard billboard,
            LocalDate start,
            LocalDate end
    ) {

        // ✅ Correct filtering: APPROVED + PAID
        List<Booking> bookings =
                bookingRepo.findByBillboard_IdAndStatusAndPaymentStatus(
                        billboard.getId(),
                        BookingStatus.APPROVED,
                        PaymentStatus.PAID
                );

        Map<LocalDate, Double> revenueMap = new HashMap<>();

        for (Booking b : bookings) {
            long days = ChronoUnit.DAYS.between(
                    b.getStartDate(),
                    b.getEndDate()
            ) + 1;

            double perDayRevenue = b.getTotalPrice() / days;

            LocalDate d = b.getStartDate();
            while (!d.isAfter(b.getEndDate())) {
                revenueMap.merge(d, perDayRevenue, Double::sum);
                d = d.plusDays(1);
            }
        }

        List<OwnerCalendarDayResponse> result = new ArrayList<>();
        LocalDate d = start;

        while (!d.isAfter(end)) {
            if (revenueMap.containsKey(d)) {
                result.add(new OwnerCalendarDayResponse(
                        d, "BOOKED", revenueMap.get(d)
                ));
            } else {
                result.add(new OwnerCalendarDayResponse(
                        d, "AVAILABLE", 0.0
                ));
            }
            d = d.plusDays(1);
        }

        return result;
    }
}

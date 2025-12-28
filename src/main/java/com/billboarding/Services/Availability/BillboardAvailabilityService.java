package com.billboarding.Services.Availability;


import com.billboarding.DTO.Availabitlity.BillboardAvailabilityResponse;
import com.billboarding.ENUM.BookingStatus;
import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Entity.OWNER.Billboard;
import com.billboarding.Repository.BillBoard.BillboardRepository;
import com.billboarding.Repository.Booking.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
@Service
@RequiredArgsConstructor
public class BillboardAvailabilityService {

    private final BillboardRepository billboardRepo;
    private final BookingRepository bookingRepo;

    public List<BillboardAvailabilityResponse> getAvailability(
            Long billboardId,
            LocalDate start,
            LocalDate end
    ) {

        Billboard billboard = billboardRepo.findById(billboardId)
                .orElseThrow(() -> new RuntimeException("Billboard not found"));

        List<BillboardAvailabilityResponse> result = new ArrayList<>();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {

            long approvedCount =
                    bookingRepo.countByBillboardAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                            billboard,
                            BookingStatus.APPROVED,
                            date,
                            date
                    );

            boolean booked = approvedCount > 0;

            String status = booked ? "BOOKED" : "AVAILABLE";

            Double price = null;

            if (!booked) {
                double base = billboard.getPricePerDay();
                double finalPrice = base;

                // Weekend surge
                if (date.getDayOfWeek() == DayOfWeek.SATURDAY ||
                        date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    finalPrice *= 1.2;
                }

                // High demand surge
                if (approvedCount > 5) {
                    finalPrice *= 1.3;
                }

                price = Math.round(finalPrice * 100.0) / 100.0;
            }

            result.add(new BillboardAvailabilityResponse(price, date, status));
        }

        return result;
    }
}

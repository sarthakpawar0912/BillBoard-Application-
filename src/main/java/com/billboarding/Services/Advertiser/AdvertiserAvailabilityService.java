package com.billboarding.Services.Advertiser;

import com.billboarding.DTO.Availabitlity.BillboardAvailabilityResponse;
import com.billboarding.ENUM.BookingStatus;
import com.billboarding.Entity.OWNER.Billboard;
import com.billboarding.Repository.BillBoard.BillboardRepository;
import com.billboarding.Repository.Booking.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AdvertiserAvailabilityService {

    private final BillboardRepository billboardRepo;
    private final BookingRepository bookingRepo;

    public BillboardAvailabilityResponse getAvailability(
            Long billboardId,
            LocalDate date
    ) {

        Billboard billboard = billboardRepo.findById(billboardId)
                .orElseThrow(() -> new RuntimeException("Billboard not found"));

        // Check if already booked
        long approvedCount = bookingRepo.countBookingsOnDate(
                billboard,
                BookingStatus.APPROVED,
                date
        );

        boolean booked = approvedCount > 0;

        double finalPrice = calculateSurgePrice(
                billboard,
                date,
                approvedCount
        );

        return new BillboardAvailabilityResponse(
                finalPrice,
                date,
                booked ? "BOOKED" : "AVAILABLE"
                );
    }

    // ⭐ CORE SURGE LOGIC ⭐
    private double calculateSurgePrice(
            Billboard billboard,
            LocalDate date,
            long approvedCount
    ) {
        double price = billboard.getPricePerDay();

        // Weekend surge
        if (date.getDayOfWeek() == DayOfWeek.SATURDAY ||
            date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            price *= 1.2;
        }

        // High demand surge
        if (approvedCount > 5) {
            price *= 1.3;
        }

        return Math.round(price * 100.0) / 100.0;
    }
}

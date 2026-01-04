package com.billboarding.Services.Availability;

import com.billboarding.DTO.Availabitlity.BillboardAvailabilityResponse;
import com.billboarding.Entity.OWNER.Billboard;
import com.billboarding.Repository.BillBoard.BillboardRepository;
import com.billboarding.Repository.Booking.BookingRepository;
import com.billboarding.Services.SmartPricing.SmartPricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillboardAvailabilityService {

    private final BillboardRepository billboardRepo;
    private final BookingRepository bookingRepo;
    private final SmartPricingService pricingService;

    public List<BillboardAvailabilityResponse> getAvailability(
            Long billboardId,
            LocalDate start,
            LocalDate end
    ) {
        Billboard billboard = billboardRepo.findById(billboardId)
                .orElseThrow(() -> new RuntimeException("Billboard not found"));

        List<BillboardAvailabilityResponse> result = new ArrayList<>();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {

            // Check if booked (APPROVED booking exists for this date)
            boolean booked = bookingRepo.countBookingsOnDate(
                    billboard,
                    com.billboarding.ENUM.BookingStatus.APPROVED,
                    date
            ) > 0;

            // Check if pending (PENDING booking exists for this date)
            boolean pending = bookingRepo.countBookingsOnDate(
                    billboard,
                    com.billboarding.ENUM.BookingStatus.PENDING,
                    date
            ) > 0;

            // Determine status
            String status;
            if (booked) {
                status = "BOOKED";
            } else if (pending) {
                status = "PENDING";
            } else {
                status = "AVAILABLE";
            }

            // Calculate price for single day (use same date for start and end)
            double dayPrice = pricingService.calculateBasePrice(billboard, date, date);

            result.add(new BillboardAvailabilityResponse(
                    date,
                    status,
                    billboard.getPricePerDay(),
                    dayPrice
            ));
        }
        return result;
    }
}

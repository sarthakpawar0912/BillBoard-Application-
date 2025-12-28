package com.billboarding.Services.Availability;

import com.billboarding.ENUM.BookingStatus;
import com.billboarding.Entity.OWNER.Billboard;
import com.billboarding.Repository.BillBoard.BillboardRepository;
import com.billboarding.Repository.Booking.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final BillboardRepository billboardRepo;
    private final BookingRepository bookingRepo;

    public boolean isAvailable(Long billboardId, LocalDate start, LocalDate end) {

        Billboard billboard = billboardRepo.findById(billboardId)
                .orElseThrow(() -> new RuntimeException("Billboard not found"));

        return !bookingRepo.existsByBillboardAndStatusInAndEndDateGreaterThanEqualAndStartDateLessThanEqual(
                billboard,
                List.of(BookingStatus.PENDING, BookingStatus.APPROVED),
                start,
                end
        );
    }
}

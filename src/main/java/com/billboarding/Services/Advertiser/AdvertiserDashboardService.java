package com.billboarding.Services.Advertiser;

import com.billboarding.DTO.Advertiser.AdvertiserDashboardResponse;
import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Entity.User;
import com.billboarding.ENUM.BookingStatus;
import com.billboarding.Repository.Advertiser.FavouriteBillboardRepository;
import com.billboarding.Repository.Booking.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdvertiserDashboardService {

    private final BookingRepository bookingRepository;
    private final FavouriteBillboardRepository favRepo;

    public AdvertiserDashboardResponse getDashboard(User advertiser) {

        List<Booking> bookings = bookingRepository.findByAdvertiser(advertiser);

        AdvertiserDashboardResponse res = new AdvertiserDashboardResponse();
        res.setTotalBookings(bookings.size());
        res.setPending(bookings.stream().filter(b -> b.getStatus() == BookingStatus.PENDING).count());
        res.setApproved(bookings.stream().filter(b -> b.getStatus() == BookingStatus.APPROVED).count());
        res.setRejected(bookings.stream().filter(b -> b.getStatus() == BookingStatus.REJECTED).count());
        res.setCancelled(bookings.stream().filter(b -> b.getStatus() == BookingStatus.CANCELLED).count());

        res.setRecentBookings(bookings.stream().limit(5).toList());

        // favourites
        res.setFavouriteBillboards(
                favRepo.findByAdvertiser(advertiser)
                        .stream()
                        .map(f -> f.getBillboard())
                        .toList()
        );

        return res;
    }
}

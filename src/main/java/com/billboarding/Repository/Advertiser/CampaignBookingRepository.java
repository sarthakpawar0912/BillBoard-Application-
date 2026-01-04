package com.billboarding.Repository.Advertiser;

import com.billboarding.Entity.Advertiser.Campaign;
import com.billboarding.Entity.Advertiser.CampaignBooking;
import com.billboarding.Entity.Bookings.Booking;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CampaignBookingRepository
        extends JpaRepository<CampaignBooking, Long> {

    List<CampaignBooking> findByCampaign(Campaign campaign);

    boolean existsByCampaignAndBooking(Campaign campaign, Booking booking);

    Optional<CampaignBooking> findByCampaignAndBooking(Campaign campaign, Booking booking);

    @Modifying
    @Transactional
    @Query("DELETE FROM CampaignBooking cb WHERE cb.campaign.id = :campaignId")
    void deleteByCampaignId(Long campaignId);

    @Modifying
    @Transactional
    @Query("DELETE FROM CampaignBooking cb WHERE cb.booking.id = :bookingId")
    void deleteByBookingId(Long bookingId);

    @Modifying
    @Transactional
    @Query("DELETE FROM CampaignBooking cb WHERE cb.booking.id IN :bookingIds")
    void deleteByBookingIdIn(List<Long> bookingIds);
}

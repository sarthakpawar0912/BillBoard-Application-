package com.billboarding.Repository.Advertiser;

import com.billboarding.Entity.Advertiser.Campaign;
import com.billboarding.Entity.Advertiser.CampaignBooking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignBookingRepository
        extends JpaRepository<CampaignBooking, Long> {

    List<CampaignBooking> findByCampaign(Campaign campaign);
}

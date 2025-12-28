package com.billboarding.Repository.Advertiser;

import com.billboarding.Entity.Advertiser.Campaign;
import com.billboarding.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findByAdvertiser(User advertiser);
}

package com.billboarding.Repository.Advertiser;

import com.billboarding.Entity.Advertiser.AdvertiserProfile;
import com.billboarding.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdvertiserProfileRepository
        extends JpaRepository<AdvertiserProfile, Long> {

    Optional<AdvertiserProfile> findByAdvertiser(User advertiser);
}

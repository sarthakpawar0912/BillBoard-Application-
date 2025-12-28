package com.billboarding.Repository.Advertiser;

import com.billboarding.Entity.Advertiser.FavouriteBillboard;
import com.billboarding.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavouriteBillboardRepository
        extends JpaRepository<FavouriteBillboard, Long> {

    List<FavouriteBillboard> findByAdvertiser(User advertiser);

    boolean existsByAdvertiserAndBillboard_Id(User advertiser, Long billboardId);

    Optional<FavouriteBillboard> findByIdAndAdvertiser(Long id, User advertiser);
}

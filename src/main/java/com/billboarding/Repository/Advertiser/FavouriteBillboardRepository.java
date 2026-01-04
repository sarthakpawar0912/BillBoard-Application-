package com.billboarding.Repository.Advertiser;

import com.billboarding.Entity.Advertiser.FavouriteBillboard;
import com.billboarding.Entity.OWNER.Billboard;
import com.billboarding.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FavouriteBillboardRepository
        extends JpaRepository<FavouriteBillboard, Long> {

    List<FavouriteBillboard> findByAdvertiser(User advertiser);

    // Favourites with billboard images and owner
    @Query("SELECT DISTINCT f FROM FavouriteBillboard f LEFT JOIN FETCH f.billboard b LEFT JOIN FETCH b.imagePaths LEFT JOIN FETCH b.owner WHERE f.advertiser = :advertiser")
    List<FavouriteBillboard> findByAdvertiserWithDetails(@Param("advertiser") User advertiser);

    boolean existsByAdvertiserAndBillboard_Id(User advertiser, Long billboardId);

    Optional<FavouriteBillboard> findByIdAndAdvertiser(Long id, User advertiser);

    // Delete all favourites for a billboard (used when deleting billboard)
    void deleteByBillboard(Billboard billboard);
}

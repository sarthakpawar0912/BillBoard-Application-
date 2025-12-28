package com.billboarding.Repository.BillBoard;
import com.billboarding.DTO.HeatMaps.HeatmapPoint;
import com.billboarding.DTO.HeatMaps.RevenueMapPoint;
import com.billboarding.Entity.OWNER.Billboard;
import com.billboarding.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BillboardRepository extends JpaRepository<Billboard, Long> {
    // All boards by owner
    List<Billboard> findByOwner(User owner);

    // All currently marked as available = true
    List<Billboard> findByAvailableTrue();

    // Advertiser: only visible & mapped billboards
    List<Billboard> findByAvailableTrueAndLatitudeNotNullAndLongitudeNotNull();

    // Owner: only my billboards on map
    List<Billboard> findByOwnerAndLatitudeNotNullAndLongitudeNotNull(User owner);
    @Query("""
    SELECT b FROM Billboard b
    WHERE b.available = true
    AND b.latitude IS NOT NULL
    AND b.longitude IS NOT NULL
    AND (
        6371 * acos(
            cos(radians(:lat)) * cos(radians(b.latitude)) *
            cos(radians(b.longitude) - radians(:lng)) +
            sin(radians(:lat)) * sin(radians(b.latitude))
        )
    ) <= :radius
    """)
    List<Billboard> findNearby(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radius") double radius
    );

    @Query("""
SELECT new com.billboarding.DTO.HeatMaps.HeatmapPoint(
    b.latitude,
    b.longitude,
    COUNT(k)
)
FROM Booking k
JOIN k.billboard b
WHERE k.status = 'APPROVED'
GROUP BY b.latitude, b.longitude
""")
    List<HeatmapPoint> getHeatmapData();


    @Query("""
SELECT new com.billboarding.DTO.HeatMaps.RevenueMapPoint(
    b.latitude,
    b.longitude,
    SUM(k.totalPrice)
)
FROM Booking k
JOIN k.billboard b
WHERE b.owner = :owner
AND k.status = 'APPROVED'
GROUP BY b.latitude, b.longitude
""")
    List<RevenueMapPoint> getRevenueMap(@Param("owner") User owner);


}


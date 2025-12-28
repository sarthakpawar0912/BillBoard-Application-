package com.billboarding.Controller.Advertiser;

import com.billboarding.DTO.HeatMaps.HeatmapPoint;
import com.billboarding.DTO.HeatMaps.RevenueMapPoint;
import com.billboarding.Entity.OWNER.Billboard;
import com.billboarding.Entity.User;
import com.billboarding.Repository.BillBoard.BillboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/advertiser/map")
@RequiredArgsConstructor
public class AdvertiserMapController {

    private final BillboardRepository billboardRepository;

    @GetMapping("/nearby")
    public ResponseEntity<List<Billboard>> getNearbyBillboards(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5") double radius
    ) {
        return ResponseEntity.ok(
                billboardRepository.findNearby(lat, lng, radius)
        );
    }

    @GetMapping("/heatmap")
    public ResponseEntity<List<HeatmapPoint>> getHeatmap() {
        return ResponseEntity.ok(billboardRepository.getHeatmapData());
    }




}

package com.billboarding.Controller.Owner;

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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/owner/map")
@RequiredArgsConstructor
public class OwnerMapController {

    private final BillboardRepository billboardRepository;

    @GetMapping("/billboards")
    public ResponseEntity<List<Billboard>> getOwnerMap(Authentication auth) {
        User owner = (User) auth.getPrincipal();
        return ResponseEntity.ok(
            billboardRepository.findByOwnerAndLatitudeNotNullAndLongitudeNotNull(owner)
        );
    }

    @GetMapping("/analytics")
    public ResponseEntity<List<RevenueMapPoint>> getOwnerAnalytics(Authentication auth) {
        User owner = (User) auth.getPrincipal();
        return ResponseEntity.ok(billboardRepository.getRevenueMap(owner));
    }




}

package com.billboarding.DTO.Booking;

import com.billboarding.ENUM.BillboardType;
import com.billboarding.Entity.OWNER.Billboard;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Billboard response DTO - hides owner's sensitive data
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillboardResponseDTO {

    private Long id;
    private String title;
    private String location;
    private Double latitude;
    private Double longitude;
    private Double pricePerDay;
    private String size;
    private boolean available;
    private BillboardType type;
    private UserSummaryDTO owner;
    private LocalDateTime createdAt;
    private List<String> imagePaths;

    public static BillboardResponseDTO fromEntity(Billboard billboard) {
        if (billboard == null) return null;

        return BillboardResponseDTO.builder()
                .id(billboard.getId())
                .title(billboard.getTitle())
                .location(billboard.getLocation())
                .latitude(billboard.getLatitude())
                .longitude(billboard.getLongitude())
                .pricePerDay(billboard.getPricePerDay())
                .size(billboard.getSize())
                .available(billboard.isAvailable())
                .type(billboard.getType())
                .owner(UserSummaryDTO.fromEntity(billboard.getOwner()))
                .createdAt(billboard.getCreatedAt())
                .imagePaths(billboard.getImagePaths())
                .build();
    }

    public static List<BillboardResponseDTO> fromEntityList(List<Billboard> billboards) {
        return billboards.stream()
                .map(BillboardResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
}

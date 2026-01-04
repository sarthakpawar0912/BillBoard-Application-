package com.billboarding.Entity.OWNER;

import com.billboarding.ENUM.BillboardType;
import com.billboarding.Entity.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "billboards", indexes = {
    @Index(name = "idx_billboard_owner", columnList = "owner_id"),
    @Index(name = "idx_billboard_available", columnList = "available"),
    @Index(name = "idx_billboard_location", columnList = "latitude, longitude"),
    @Index(name = "idx_billboard_type", columnList = "type")
})
public class Billboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String location;

    private Double latitude;
    private Double longitude;

    private Double pricePerDay;
    private String size;

    private boolean available;

    /**
     * Admin-level block flag. When true, the billboard is blocked by admin
     * and owner cannot unblock it. Only admin can clear this flag.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean adminBlocked = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillboardType type; // STATIC / LED / DIGITAL / NEON

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User owner;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.available = true;
    }

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "billboard_images",
            joinColumns = @JoinColumn(name = "billboard_id")
    )
    @Column(name = "image_path", length = 1024)
    @Builder.Default
    private List<String> imagePaths = new ArrayList<>();
}

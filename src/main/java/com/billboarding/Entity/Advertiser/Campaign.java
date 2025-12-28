package com.billboarding.Entity.Advertiser;
import com.billboarding.ENUM.CampaignStatus;
import com.billboarding.Entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "campaigns")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Campaign {

    @Id @GeneratedValue
    private Long id;

    private String name;

    @ManyToOne
    private User advertiser;

    @Enumerated(EnumType.STRING)
    private CampaignStatus status;

    private Integer billboards;
    private LocalDate startDate;
    private LocalDate endDate;

    private BigDecimal budget;
    private BigDecimal spent;

    private Long impressions;

    @ElementCollection
    private List<String> cities;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

package com.billboarding.Entity.Advertiser;

import com.billboarding.ENUM.CampaignStatus;
import com.billboarding.Entity.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "campaigns")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User advertiser;

    @Enumerated(EnumType.STRING)
    private CampaignStatus status;

    private Double budget;
    private Double spent;

    private LocalDate startDate;
    private LocalDate endDate;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<String> cities;

    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = CampaignStatus.SCHEDULED;
        if (spent == null) spent = 0.0;
    }
}

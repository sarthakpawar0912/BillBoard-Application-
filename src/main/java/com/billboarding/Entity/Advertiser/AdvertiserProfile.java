package com.billboarding.Entity.Advertiser;

import com.billboarding.Entity.User;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "advertiser_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdvertiserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User advertiser;

    private String companyName;
    private String industry;
    private String website;
    private String phone;

    // 🔥 ACTUAL IMAGE STORAGE
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] logoData;

    private String logoType; // image/png, image/jpeg
}

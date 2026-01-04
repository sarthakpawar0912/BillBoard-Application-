package com.billboarding.Entity.OWNER.profile;

import com.billboarding.Entity.User;
import jakarta.persistence.*;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "owner_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User owner;

    private String companyName;

    private String gstNumber;

    private String businessAddress;

    private String phone;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] logoData;

    private String logoType;
}

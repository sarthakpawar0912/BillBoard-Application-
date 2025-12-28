package com.billboarding.Entity.Advertiser;

import com.billboarding.Entity.OWNER.Billboard;
import com.billboarding.Entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "favourite_billboards")
public class FavouriteBillboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User advertiser;

    @ManyToOne
    private Billboard billboard;
}

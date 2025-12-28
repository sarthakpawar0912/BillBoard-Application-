package com.billboarding.Entity.Advertiser;

import com.billboarding.Entity.Bookings.Booking;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "campaign_bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Campaign campaign;

    @ManyToOne
    private Booking booking;
}

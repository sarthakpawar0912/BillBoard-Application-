package com.billboarding.Entity.Advertiser;

import com.billboarding.Entity.Bookings.Booking;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "campaign_bookings",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"campaign_id", "booking_id"}
        )
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CampaignBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Campaign campaign;

    @ManyToOne(optional = false)
    private Booking booking;
}

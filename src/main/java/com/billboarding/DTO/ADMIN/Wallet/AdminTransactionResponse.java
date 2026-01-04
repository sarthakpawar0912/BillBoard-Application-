package com.billboarding.DTO.ADMIN.Wallet;

import com.billboarding.ENUM.TxType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminTransactionResponse {

    private Long id;
    private Double amount;
    private TxType type;
    private String reference;
    private String description;
    private Long bookingId;
    private Long ownerId;
    private String ownerName;
    private Double balanceAfter;
    private LocalDateTime time;
}

package com.billboarding.Services.Payment;

import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Entity.Payment.PaymentSplit;
import com.billboarding.Repository.Payment.PaymentSplitRepository;
import com.billboarding.Services.Admin.Wallet.AdminWalletService;
import com.billboarding.Services.Owner.Wallet.OwnerWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentSplitService {

    private final PaymentSplitRepository repo;
    private final OwnerWalletService ownerWalletService;
    private final AdminWalletService adminWalletService;

    @Transactional
    public PaymentSplit createSplit(Booking booking) {

        // IDEMPOTENCY CHECK: If split already exists for this booking, return it
        Optional<PaymentSplit> existingSplit = repo.findByBooking(booking);
        if (existingSplit.isPresent()) {
            System.out.println("[PaymentSplitService] Split already exists for booking #" + booking.getId() + ". Skipping.");
            return existingSplit.get();
        }

        System.out.println("[PaymentSplitService] Creating new split for booking #" + booking.getId());

        PaymentSplit split = PaymentSplit.builder()
                .booking(booking)
                .ownerAmount(booking.getBaseAmount())
                .platformCommission(booking.getCommissionAmount())
                .gstAmount(booking.getGstAmount())
                .settled(false)
                .build();

        PaymentSplit saved = repo.save(split);

        // CREDIT OWNER WALLET with base amount
        try {
            ownerWalletService.credit(
                    booking.getBillboard().getOwner(),
                    booking.getBaseAmount(),
                    "BOOKING#" + booking.getId()
            );
            System.out.println("[PaymentSplitService] Owner wallet credited: " + booking.getBaseAmount());
        } catch (Exception e) {
            System.err.println("[PaymentSplitService] Error crediting owner wallet: " + e.getMessage());
            throw e; // Re-throw to rollback transaction
        }

        // CREDIT ADMIN WALLET with platform commission
        try {
            adminWalletService.credit(
                    booking.getCommissionAmount(),
                    "COMMISSION#BOOKING#" + booking.getId()
            );
            System.out.println("[PaymentSplitService] Admin wallet credited: " + booking.getCommissionAmount());
        } catch (Exception e) {
            System.err.println("[PaymentSplitService] Error crediting admin wallet: " + e.getMessage());
            throw e; // Re-throw to rollback transaction
        }

        System.out.println("[PaymentSplitService] Split created successfully for booking #" + booking.getId());

        return saved;
    }

    @Transactional
    public void markSettled(Booking booking) {
        PaymentSplit split = repo.findByBooking(booking).orElseThrow();
        split.setSettled(true);
        repo.save(split);
    }
}

package com.billboarding.Services.BillBoard;

import com.billboarding.Entity.Bookings.Booking;
import com.billboarding.Entity.OWNER.Billboard;
import com.billboarding.Entity.User;
import com.billboarding.Repository.Admin.Wallet.AdminWalletTransactionRepository;
import com.billboarding.Repository.Advertiser.CampaignBookingRepository;
import com.billboarding.Repository.Advertiser.FavouriteBillboardRepository;
import com.billboarding.Repository.Audit.BookingAuditRepository;
import com.billboarding.Repository.BillBoard.BillboardRepository;
import com.billboarding.Repository.Booking.BookingRepository;
import com.billboarding.Repository.Payment.InvoiceRepository;
import com.billboarding.Repository.Payment.PaymentHistoryRepository;
import com.billboarding.Repository.Payment.PaymentSplitRepository;
import com.billboarding.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillboardService {

    private final BillboardRepository billboardRepo;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final FavouriteBillboardRepository favouriteBillboardRepository;
    private final CampaignBookingRepository campaignBookingRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final PaymentSplitRepository paymentSplitRepository;
    private final InvoiceRepository invoiceRepository;
    private final BookingAuditRepository bookingAuditRepository;
    private final AdminWalletTransactionRepository adminWalletTransactionRepository;

    private static final String BASE_DIR = "uploads/billboards/";

    // CREATE BILLBOARD
    public Billboard createBillboard(Long ownerId, Billboard billboard) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        billboard.setOwner(owner);
        return billboardRepo.save(billboard);
    }

    // UPDATE BILLBOARD
    // NOTE: This method preserves the existing 'available' flag.
    // Use setAvailable() separately or the toggle endpoint to change availability.
    public Billboard updateBillboard(Long id, Billboard updated) {
        Billboard board = billboardRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Billboard not found"));

        board.setTitle(updated.getTitle());
        board.setLocation(updated.getLocation());
        board.setPricePerDay(updated.getPricePerDay());
        board.setSize(updated.getSize());
        // FIXED: Don't overwrite 'available' with default false value
        // The 'available' field should only be changed through dedicated toggle endpoints
        // board.setAvailable(updated.isAvailable()); // REMOVED - was causing billboards to disappear

        if (updated.getType() != null)
            board.setType(updated.getType());

        if (updated.getLatitude() != null)
            board.setLatitude(updated.getLatitude());

        if (updated.getLongitude() != null)
            board.setLongitude(updated.getLongitude());

        return billboardRepo.save(board);
    }

    @Transactional
    public void deleteBillboard(Long id, Long ownerId, boolean force) {
        Billboard billboard = billboardRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Billboard not found"));

        // Verify ownership
        if (!billboard.getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("You can only delete your own billboards");
        }

        // Check if there are any bookings for this billboard
        List<Booking> bookings = bookingRepository.findByBillboard_Id(id);
        if (!bookings.isEmpty() && !force) {
            throw new RuntimeException(
                    "Cannot delete billboard with existing bookings. " +
                    "Found " + bookings.size() + " booking(s) associated with this billboard. " +
                    "Use force=true to delete anyway."
            );
        }

        // If force delete, remove all bookings first (and their dependencies)
        if (!bookings.isEmpty() && force) {
            // Get booking IDs
            List<Long> bookingIds = bookings.stream()
                    .map(Booking::getId)
                    .toList();

            // Delete all dependent records in correct order (child tables first)

            // 1. Delete campaign_bookings (references bookings)
            campaignBookingRepository.deleteByBookingIdIn(bookingIds);

            // 2. Delete payment_history (references bookings)
            paymentHistoryRepository.deleteByBookingIdIn(bookingIds);

            // 3. Delete payment_splits (references bookings)
            paymentSplitRepository.deleteByBookingIdIn(bookingIds);

            // 4. Delete invoices (references bookings)
            invoiceRepository.deleteByBookingIdIn(bookingIds);

            // 5. Delete booking_audit (references bookings)
            bookingAuditRepository.deleteByBookingIdIn(bookingIds);

            // 6. Nullify booking references in admin_wallet_transactions (keep transaction history)
            adminWalletTransactionRepository.nullifyBookingIdIn(bookingIds);

            // Now delete the bookings
            bookingRepository.deleteAll(bookings);
        }

        // Delete all favourites for this billboard first
        favouriteBillboardRepository.deleteByBillboard(billboard);

        // Now safe to delete the billboard
        billboardRepo.deleteById(id);
    }

    public List<Billboard> getOwnerBillboards(Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        return billboardRepo.findByOwnerWithImages(owner);
    }

    public Billboard getBillboardById(Long id) {
        return billboardRepo.findById(id).orElse(null);
    }

    public Billboard save(Billboard billboard) {
        return billboardRepo.save(billboard);
    }

    // IMAGE UPLOAD
    public List<String> saveImages(Long billboardId, List<MultipartFile> files) throws IOException {

        if (files == null || files.size() < 3)
            throw new RuntimeException("Minimum 3 images required");

        String dirPath = BASE_DIR + billboardId + "/";
        File folder = new File(dirPath);
        folder.mkdirs();

        List<String> storedPaths = new ArrayList<>();

        for (MultipartFile file : files) {

            String original = file.getOriginalFilename();
            if (original == null) original = "image.jpg";

            String fileName = System.currentTimeMillis()
                    + "_" + original.replace(" ", "_");

            String fullPath = dirPath + fileName;

            Files.write(Paths.get(fullPath), file.getBytes());
            storedPaths.add(fullPath);
        }

        return storedPaths;
    }
}

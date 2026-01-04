package com.billboarding.Controller.Admin;

import com.billboarding.Entity.OWNER.Billboard;
import com.billboarding.Repository.BillBoard.BillboardRepository;
import com.billboarding.Services.BillBoard.BillboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin endpoints for managing billboards
 * All endpoints require ADMIN role
 */
@RestController
@RequestMapping("/api/admin/billboards")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminBillboardController {

    private final BillboardRepository billboardRepository;
    private final BillboardService billboardService;

    /**
     * Get all billboards in the system
     * GET /api/admin/billboards
     */
    @GetMapping
    public ResponseEntity<List<Billboard>> getAllBillboards() {
        List<Billboard> billboards = billboardRepository.findAllWithDetails();
        return ResponseEntity.ok(billboards);
    }

    /**
     * Get a specific billboard by ID
     * GET /api/admin/billboards/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Billboard> getBillboard(@PathVariable Long id) {
        Billboard billboard = billboardRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Billboard not found"));
        return ResponseEntity.ok(billboard);
    }

    /**
     * Toggle billboard availability (enable/disable)
     * POST /api/admin/billboards/{id}/toggle-availability
     */
    @PostMapping("/{id}/toggle-availability")
    public ResponseEntity<Billboard> toggleAvailability(@PathVariable Long id) {
        Billboard billboard = billboardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Billboard not found"));

        billboard.setAvailable(!billboard.isAvailable());
        Billboard updated = billboardRepository.save(billboard);

        System.out.println("[AdminBillboard] Toggled availability for billboard #" + id + " to: " + updated.isAvailable());
        return ResponseEntity.ok(updated);
    }

    /**
     * Enable a billboard (set available = true)
     * POST /api/admin/billboards/{id}/enable
     */
    @PostMapping("/{id}/enable")
    public ResponseEntity<Billboard> enableBillboard(@PathVariable Long id) {
        Billboard billboard = billboardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Billboard not found"));

        billboard.setAvailable(true);
        Billboard updated = billboardRepository.save(billboard);

        System.out.println("[AdminBillboard] Enabled billboard #" + id);
        return ResponseEntity.ok(updated);
    }

    /**
     * Disable a billboard (set available = false)
     * POST /api/admin/billboards/{id}/disable
     */
    @PostMapping("/{id}/disable")
    public ResponseEntity<Billboard> disableBillboard(@PathVariable Long id) {
        Billboard billboard = billboardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Billboard not found"));

        billboard.setAvailable(false);
        Billboard updated = billboardRepository.save(billboard);

        System.out.println("[AdminBillboard] Disabled billboard #" + id);
        return ResponseEntity.ok(updated);
    }

    /**
     * Admin Block a billboard (sets adminBlocked = true AND available = false)
     * This prevents owner from unblocking until admin lifts the block.
     * POST /api/admin/billboards/{id}/block
     */
    @PostMapping("/{id}/block")
    public ResponseEntity<Billboard> blockBillboard(@PathVariable Long id) {
        Billboard billboard = billboardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Billboard not found"));

        billboard.setAdminBlocked(true);
        billboard.setAvailable(false);
        Billboard updated = billboardRepository.save(billboard);

        System.out.println("[AdminBillboard] ADMIN BLOCKED billboard #" + id);
        return ResponseEntity.ok(updated);
    }

    /**
     * Admin Unblock a billboard (sets adminBlocked = false)
     * Note: This does NOT automatically make billboard available - owner controls that.
     * POST /api/admin/billboards/{id}/unblock
     */
    @PostMapping("/{id}/unblock")
    public ResponseEntity<Billboard> unblockBillboard(@PathVariable Long id) {
        Billboard billboard = billboardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Billboard not found"));

        billboard.setAdminBlocked(false);
        // Note: We leave 'available' as-is. Owner can now control it.
        Billboard updated = billboardRepository.save(billboard);

        System.out.println("[AdminBillboard] ADMIN UNBLOCKED billboard #" + id);
        return ResponseEntity.ok(updated);
    }

    /**
     * Update billboard price
     * PUT /api/admin/billboards/{id}/price
     */
    @PutMapping("/{id}/price")
    public ResponseEntity<Billboard> updatePrice(
            @PathVariable Long id,
            @RequestBody Map<String, Double> body) {

        Double newPrice = body.get("price");
        if (newPrice == null || newPrice < 0) {
            throw new RuntimeException("Invalid price");
        }

        Billboard billboard = billboardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Billboard not found"));

        billboard.setPricePerDay(newPrice);
        Billboard updated = billboardRepository.save(billboard);

        System.out.println("[AdminBillboard] Updated price for billboard #" + id + " to: " + newPrice);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a billboard
     * DELETE /api/admin/billboards/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteBillboard(@PathVariable Long id) {
        if (!billboardRepository.existsById(id)) {
            throw new RuntimeException("Billboard not found");
        }

        billboardRepository.deleteById(id);
        System.out.println("[AdminBillboard] Deleted billboard #" + id);

        return ResponseEntity.ok(Map.of(
                "message", "Billboard deleted successfully",
                "id", String.valueOf(id)
        ));
    }
}

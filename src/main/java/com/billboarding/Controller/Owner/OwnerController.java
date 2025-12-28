package com.billboarding.Controller.Owner;

import com.billboarding.Entity.OWNER.Billboard;
import com.billboarding.Entity.User;
import com.billboarding.Services.BillBoard.BillboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/owner/billboards")
@RequiredArgsConstructor
public class OwnerController {

    private final BillboardService billboardService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Billboard billboard, Authentication auth) {

        if (billboard.getType() == null)
            return ResponseEntity.badRequest().body("Billboard type is required");

        if (billboard.getLatitude() == null || billboard.getLongitude() == null)
            return ResponseEntity.badRequest().body("Latitude & Longitude required");

        User owner = (User) auth.getPrincipal();
        return ResponseEntity.ok(billboardService.createBillboard(owner.getId(), billboard));
    }

    @GetMapping
    public ResponseEntity<List<Billboard>> myBoards(Authentication auth) {
        User owner = (User) auth.getPrincipal();
        return ResponseEntity.ok(billboardService.getOwnerBillboards(owner.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Billboard updated) {
        return ResponseEntity.ok(billboardService.updateBillboard(id, updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        billboardService.deleteBillboard(id);
        return ResponseEntity.ok("Billboard deleted");
    }

    @PostMapping(value = "/{id}/upload-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImages(
            @PathVariable Long id,
            @RequestParam("images") List<MultipartFile> images,
            Authentication auth
    ) throws IOException {

        User owner = (User) auth.getPrincipal();
        Billboard board = billboardService.getBillboardById(id);

        if (board == null)
            return ResponseEntity.badRequest().body("Billboard not found");

        if (!board.getOwner().getId().equals(owner.getId()))
            return ResponseEntity.status(403).body("Not your billboard");

        List<String> paths = billboardService.saveImages(id, images);

        if (board.getImagePaths() == null)
            board.setImagePaths(new ArrayList<>());

        board.getImagePaths().addAll(paths);

        return ResponseEntity.ok(billboardService.save(board));
    }
}

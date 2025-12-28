package com.billboarding.Services.BillBoard;

import com.billboarding.Entity.OWNER.Billboard;
import com.billboarding.Entity.User;
import com.billboarding.Repository.BillBoard.BillboardRepository;
import com.billboarding.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

    private static final String BASE_DIR = "uploads/billboards/";

    // CREATE BILLBOARD
    public Billboard createBillboard(Long ownerId, Billboard billboard) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        billboard.setOwner(owner);
        return billboardRepo.save(billboard);
    }

    // UPDATE BILLBOARD
    public Billboard updateBillboard(Long id, Billboard updated) {
        Billboard board = billboardRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Billboard not found"));

        board.setTitle(updated.getTitle());
        board.setLocation(updated.getLocation());
        board.setPricePerDay(updated.getPricePerDay());
        board.setSize(updated.getSize());
        board.setAvailable(updated.isAvailable());

        if (updated.getType() != null)
            board.setType(updated.getType());

        if (updated.getLatitude() != null)
            board.setLatitude(updated.getLatitude());

        if (updated.getLongitude() != null)
            board.setLongitude(updated.getLongitude());

        return billboardRepo.save(board);
    }

    public void deleteBillboard(Long id) {
        billboardRepo.deleteById(id);
    }

    public List<Billboard> getOwnerBillboards(Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        return billboardRepo.findByOwner(owner);
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

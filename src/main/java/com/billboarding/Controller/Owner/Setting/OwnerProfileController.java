package com.billboarding.Controller.Owner.Setting;

import com.billboarding.DTO.OWNER.Profile.OwnerProfileDTO;
import com.billboarding.Entity.OWNER.profile.OwnerProfile;
import com.billboarding.Entity.User;
import com.billboarding.Services.Owner.profile.OwnerProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/owner/settings/profile")
@RequiredArgsConstructor
public class OwnerProfileController {

    private final OwnerProfileService service;
    private final ObjectMapper mapper;

    @GetMapping
    public ResponseEntity<?> getProfile(Authentication auth) {
        User owner = (User) auth.getPrincipal();
        return ResponseEntity.ok(service.getProfile(owner));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfile(
            @RequestPart("data") String data,
            @RequestPart(value = "logo", required = false) MultipartFile logo,
            Authentication auth
    ) throws Exception {

        User owner = (User) auth.getPrincipal();
        OwnerProfileDTO dto =
                mapper.readValue(data, OwnerProfileDTO.class);

        return ResponseEntity.ok(
                service.saveProfile(owner, dto, logo)
        );
    }

    @GetMapping("/logo")
    public ResponseEntity<byte[]> getLogo(Authentication auth) {
        User owner = (User) auth.getPrincipal();
        OwnerProfile p = service.getEntity(owner);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, p.getLogoType())
                .body(p.getLogoData());
    }

    /**
     * POST /api/owner/settings/profile/image
     * Upload profile image only (independent of profile update)
     */
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadProfileImage(
            @RequestParam("image") MultipartFile image,
            Authentication auth
    ) {
        if (image == null || image.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No image provided"));
        }

        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid file type. Please upload an image."));
        }

        if (image.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(Map.of("error", "Image size exceeds 5MB limit"));
        }

        User owner = (User) auth.getPrincipal();
        service.saveProfileImage(owner, image);

        return ResponseEntity.ok(Map.of(
                "message", "Profile image uploaded successfully",
                "hasProfileImage", true
        ));
    }

    /**
     * GET /api/owner/settings/profile/image
     * Get profile image
     */
    @GetMapping("/image")
    public ResponseEntity<byte[]> getProfileImage(Authentication auth) {
        User owner = (User) auth.getPrincipal();
        byte[] imageData = service.getProfileImage(owner);

        if (imageData == null || imageData.length == 0) {
            return ResponseEntity.notFound().build();
        }

        String contentType = service.getProfileImageType(owner);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(imageData);
    }

    /**
     * GET /api/owner/settings/profile/has-image
     * Check if owner has profile image
     */
    @GetMapping("/has-image")
    public ResponseEntity<?> hasProfileImage(Authentication auth) {
        User owner = (User) auth.getPrincipal();
        boolean hasImage = service.hasProfileImage(owner);
        return ResponseEntity.ok(Map.of("hasImage", hasImage));
    }
}

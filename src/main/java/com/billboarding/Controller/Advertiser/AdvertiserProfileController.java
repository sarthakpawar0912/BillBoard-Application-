package com.billboarding.Controller.Advertiser;

import com.billboarding.DTO.Advertiser.AdvertiserProfileDTO;
import com.billboarding.ENUM.UserRole;
import com.billboarding.Entity.Advertiser.AdvertiserProfile;
import com.billboarding.Entity.User;
import com.billboarding.Services.Advertiser.AdvertiserProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;@RestController
@RequestMapping("/api/advertiser/settings/profile")
@RequiredArgsConstructor
public class AdvertiserProfileController {

    private final AdvertiserProfileService profileService;
    private final ObjectMapper objectMapper;

    // GET PROFILE
    @GetMapping
    public ResponseEntity<?> getProfile(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(profileService.getProfile(user));
    }

    // UPDATE PROFILE + LOGO
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> updateProfile(
            @RequestPart("data") String data,
            @RequestPart(value = "logo", required = false) MultipartFile logo,
            Authentication auth
    ) throws Exception {

        User user = (User) auth.getPrincipal();

        AdvertiserProfileDTO dto =
                objectMapper.readValue(data, AdvertiserProfileDTO.class);

        return ResponseEntity.ok(
                profileService.saveProfile(user, dto, logo)
        );
    }

    // FETCH LOGO
    @GetMapping("/logo")
    public ResponseEntity<byte[]> getLogo(Authentication auth) {

        User user = (User) auth.getPrincipal();
        AdvertiserProfile profile = profileService.getProfileEntity(user);

        if (profile.getLogoData() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(profile.getLogoType()))
                .body(profile.getLogoData());
    }
}

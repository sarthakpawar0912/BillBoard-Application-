package com.billboarding.Services.Owner.profile;

import com.billboarding.DTO.OWNER.Profile.OwnerProfileDTO;
import com.billboarding.Entity.OWNER.profile.OwnerProfile;
import com.billboarding.Entity.User;
import com.billboarding.Repository.Owner.Profile.OwnerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class OwnerProfileService {

    private final OwnerProfileRepository repo;

    public OwnerProfileDTO getProfile(User owner) {

        OwnerProfile profile = repo.findByOwner(owner).orElse(null);

        return OwnerProfileDTO.builder()
                .fullName(owner.getName())
                .email(owner.getEmail())
                .phone(profile != null ? profile.getPhone() : owner.getPhone())
                .companyName(profile != null ? profile.getCompanyName() : null)
                .gstNumber(profile != null ? profile.getGstNumber() : null)
                .businessAddress(profile != null ? profile.getBusinessAddress() : null)
                .hasLogo(profile != null && profile.getLogoData() != null)
                .build();
    }

    public OwnerProfileDTO saveProfile(
            User owner,
            OwnerProfileDTO dto,
            MultipartFile logo
    ) {

        OwnerProfile profile = repo.findByOwner(owner)
                .orElse(OwnerProfile.builder().owner(owner).build());

        profile.setCompanyName(dto.getCompanyName());
        profile.setGstNumber(dto.getGstNumber());
        profile.setBusinessAddress(dto.getBusinessAddress());
        profile.setPhone(dto.getPhone());

        if (logo != null && !logo.isEmpty()) {
            try {
                profile.setLogoData(logo.getBytes());
                profile.setLogoType(logo.getContentType());
            } catch (Exception e) {
                throw new RuntimeException("Logo upload failed");
            }
        }

        repo.save(profile);
        return getProfile(owner);
    }

    public OwnerProfile getEntity(User owner) {
        return repo.findByOwner(owner)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
    }

    /**
     * Get or create profile entity for owner
     */
    public OwnerProfile getOrCreateProfile(User owner) {
        return repo.findByOwner(owner)
                .orElseGet(() -> {
                    OwnerProfile newProfile = OwnerProfile.builder()
                            .owner(owner)
                            .build();
                    return repo.save(newProfile);
                });
    }

    /**
     * Upload profile image only (without updating other fields)
     */
    public void saveProfileImage(User owner, MultipartFile image) {
        OwnerProfile profile = getOrCreateProfile(owner);

        try {
            profile.setLogoData(image.getBytes());
            profile.setLogoType(image.getContentType());
            repo.save(profile);
        } catch (Exception e) {
            throw new RuntimeException("Profile image upload failed: " + e.getMessage());
        }
    }

    /**
     * Check if owner has profile image
     */
    public boolean hasProfileImage(User owner) {
        return repo.findByOwner(owner)
                .map(profile -> profile.getLogoData() != null && profile.getLogoData().length > 0)
                .orElse(false);
    }

    /**
     * Get profile image data
     */
    public byte[] getProfileImage(User owner) {
        return repo.findByOwner(owner)
                .map(OwnerProfile::getLogoData)
                .orElse(null);
    }

    /**
     * Get profile image content type
     */
    public String getProfileImageType(User owner) {
        return repo.findByOwner(owner)
                .map(OwnerProfile::getLogoType)
                .orElse("image/jpeg");
    }
}

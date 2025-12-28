package com.billboarding.Services.Advertiser;

import com.billboarding.DTO.Advertiser.AdvertiserProfileDTO;
import com.billboarding.Entity.Advertiser.AdvertiserProfile;
import com.billboarding.Entity.User;
import com.billboarding.Repository.Advertiser.AdvertiserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;@Service
@RequiredArgsConstructor
public class AdvertiserProfileService {

    private final AdvertiserProfileRepository profileRepo;

    public AdvertiserProfileDTO getProfile(User advertiser) {

        AdvertiserProfile profile =
                profileRepo.findByAdvertiser(advertiser).orElse(null);

        return AdvertiserProfileDTO.builder()
                .fullName(advertiser.getName())
                .email(advertiser.getEmail())
                .phone(profile != null ? profile.getPhone() : advertiser.getPhone())
                .companyName(profile != null ? profile.getCompanyName() : null)
                .industry(profile != null ? profile.getIndustry() : null)
                .website(profile != null ? profile.getWebsite() : null)
                .hasLogo(profile != null && profile.getLogoData() != null)
                .build();
    }

    public AdvertiserProfileDTO saveProfile(
            User advertiser,
            AdvertiserProfileDTO dto,
            MultipartFile logo
    ) {

        AdvertiserProfile profile = profileRepo
                .findByAdvertiser(advertiser)
                .orElseGet(() -> AdvertiserProfile.builder()
                        .advertiser(advertiser)
                        .build()
                );

        profile.setCompanyName(dto.getCompanyName());
        profile.setIndustry(dto.getIndustry());
        profile.setWebsite(dto.getWebsite());
        profile.setPhone(dto.getPhone());

        if (logo != null && !logo.isEmpty()) {
            try {
                profile.setLogoData(logo.getBytes());
                profile.setLogoType(logo.getContentType());
            } catch (Exception e) {
                throw new RuntimeException("Failed to store logo", e);
            }
        }

        profileRepo.save(profile);
        return getProfile(advertiser);
    }

    public AdvertiserProfile getProfileEntity(User advertiser) {
        return profileRepo.findByAdvertiser(advertiser)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
    }
}

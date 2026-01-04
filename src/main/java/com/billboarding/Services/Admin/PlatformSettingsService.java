package com.billboarding.Services.Admin;

import com.billboarding.DTO.ADMIN.PlatformSettingsDTO;
import com.billboarding.Entity.Admin.PlatformSettings;
import com.billboarding.Repository.ADMIN.PlatformSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlatformSettingsService {

    private final PlatformSettingsRepository repo;

    // 🔹 Always return single settings row (CACHED)
    @Cacheable(value = "platformSettings", key = "'default'")
    public PlatformSettings get() {
        return repo.findById(1L)
                .orElseGet(() -> repo.save(
                        PlatformSettings.builder()
                                .id(1L)
                                .platformName("BOABP")
                                .supportEmail("support@boabp.com")
                                .commissionPercent(15.0)
                                .gstPercent(18.0)
                                .currency("INR")
                                .timezone("Asia/Kolkata")
                                .build()
                ));
    }

    @CacheEvict(value = "platformSettings", key = "'default'")
    public PlatformSettings update(PlatformSettingsDTO dto) {
        // ========== DEFENSIVE VALIDATION ==========
        if (dto == null) {
            throw new IllegalArgumentException("Settings data is required");
        }

        // Validate commission percentage
        if (dto.getCommissionPercent() == null) {
            throw new IllegalArgumentException("Commission percentage is required");
        }
        if (dto.getCommissionPercent() < 0 || dto.getCommissionPercent() > 100) {
            throw new IllegalArgumentException("Commission percentage must be between 0 and 100");
        }

        // Validate GST percentage
        if (dto.getGstPercent() == null) {
            throw new IllegalArgumentException("GST percentage is required");
        }
        if (dto.getGstPercent() < 0 || dto.getGstPercent() > 100) {
            throw new IllegalArgumentException("GST percentage must be between 0 and 100");
        }

        PlatformSettings settings = get();

        // Only update non-null values (partial updates allowed)
        if (dto.getPlatformName() != null && !dto.getPlatformName().isBlank()) {
            settings.setPlatformName(dto.getPlatformName());
        }
        if (dto.getSupportEmail() != null && !dto.getSupportEmail().isBlank()) {
            settings.setSupportEmail(dto.getSupportEmail());
        }

        settings.setCommissionPercent(dto.getCommissionPercent());
        settings.setGstPercent(dto.getGstPercent());

        if (dto.getCurrency() != null && !dto.getCurrency().isBlank()) {
            settings.setCurrency(dto.getCurrency());
        }
        if (dto.getTimezone() != null && !dto.getTimezone().isBlank()) {
            settings.setTimezone(dto.getTimezone());
        }

        System.out.println("[PlatformSettingsService] Updated settings: commission=" + dto.getCommissionPercent() + "%, GST=" + dto.getGstPercent() + "%");

        return repo.save(settings);
    }
}

package com.billboarding.Controller.Admin;

import com.billboarding.DTO.ADMIN.PlatformSettingsDTO;
import com.billboarding.Entity.Admin.PlatformSettings;
import com.billboarding.Services.Admin.PlatformSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/platform-settings")
@RequiredArgsConstructor
public class PlatformSettingsController {

    private final PlatformSettingsService service;

    // 🔹 GET platform settings
    @GetMapping
    public PlatformSettings getSettings() {
        return service.get();
    }

    // 🔹 UPDATE platform settings
    @PutMapping
    public PlatformSettings updateSettings(
            @Valid @RequestBody PlatformSettingsDTO dto
    ) {
        return service.update(dto);
    }
}

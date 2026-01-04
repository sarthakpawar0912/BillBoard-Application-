package com.billboarding.Repository.ADMIN;

import com.billboarding.Entity.Admin.PlatformSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformSettingsRepository
        extends JpaRepository<PlatformSettings, Long> {
}

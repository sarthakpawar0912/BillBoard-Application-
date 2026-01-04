package com.billboarding.Repository.Owner.Profile;

import com.billboarding.Entity.OWNER.profile.OwnerProfile;
import com.billboarding.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OwnerProfileRepository
        extends JpaRepository<OwnerProfile, Long> {

    Optional<OwnerProfile> findByOwner(User owner);
}

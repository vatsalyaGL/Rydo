package com.raydo.raydoApplication.repository;

import com.raydo.raydoApplication.entity.DriverProfile;
import com.raydo.raydoApplication.entity.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverRepository extends JpaRepository<DriverProfile, UUID> {

    Optional<DriverProfile> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    List<DriverProfile> findByVerificationStatus(VerificationStatus verificationStatus);
}
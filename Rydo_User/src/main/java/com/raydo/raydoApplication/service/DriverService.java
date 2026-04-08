package com.raydo.raydoApplication.service;


import com.raydo.raydoApplication.dto.DriverApplyDTO;
import com.raydo.raydoApplication.dto.DriverResponseDTO;
import com.raydo.raydoApplication.entity.VerificationStatus;

import java.util.List;
import java.util.UUID;

public interface DriverService {

    DriverResponseDTO applyForDriver(UUID userId, DriverApplyDTO dto);

    DriverResponseDTO getDriverProfile(UUID userId);

    DriverResponseDTO updateDriverProfile(UUID userId, DriverApplyDTO dto);

    void verifyDriver(UUID userId);

    void rejectDriver(UUID userId, String reason);

    List<DriverResponseDTO> getDrivers(VerificationStatus verificationStatus);


}
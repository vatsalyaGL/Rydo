package com.raydo.raydoApplication.service;

import com.raydo.raydoApplication.dto.DriverApplyDTO;
import com.raydo.raydoApplication.dto.DriverResponseDTO;
import com.raydo.raydoApplication.entity.*;
import com.raydo.raydoApplication.repository.DriverRepository;
import com.raydo.raydoApplication.repository.UserRepository;
import com.raydo.raydoApplication.service.DriverService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final UserRepository userRepository;

    @Override
    public DriverResponseDTO applyForDriver(UUID userId, DriverApplyDTO dto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (driverRepository.existsByUserId(userId)) {
            throw new RuntimeException("Driver profile already exists");
        }

        DriverProfile profile = DriverProfile.builder()
                .user(user)
                .licenseNumber(dto.getLicenseNumber())
                .vehicleMake(dto.getVehicleMake())
                .vehicleModel(dto.getVehicleModel())
                .vehicleYear(dto.getVehicleYear())
                .vehicleColor(dto.getVehicleColor())
                .vehiclePlate(dto.getVehiclePlate())
                .vehicleType(parseVehicleType(dto.getVehicleType()))
                .verificationStatus(VerificationStatus.PENDING)
                .cityId(dto.getCityId())
                .build();

        profile = driverRepository.save(profile);

        return mapToDTO(profile);
    }

    @Override
    public DriverResponseDTO getDriverProfile(UUID userId) {

        DriverProfile profile = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Driver profile not found"));

        return mapToDTO(profile);
    }

    @Override
    public DriverResponseDTO updateDriverProfile(UUID userId, DriverApplyDTO dto) {

        DriverProfile profile = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Driver profile not found"));

        if (dto.getVehicleMake() != null) profile.setVehicleMake(dto.getVehicleMake());
        if (dto.getVehicleModel() != null) profile.setVehicleModel(dto.getVehicleModel());
        if (dto.getVehicleColor() != null) profile.setVehicleColor(dto.getVehicleColor());
        if (dto.getVehiclePlate() != null) profile.setVehiclePlate(dto.getVehiclePlate());

        profile = driverRepository.save(profile);

        return mapToDTO(profile);
    }

    private DriverResponseDTO mapToDTO(DriverProfile profile) {

        return DriverResponseDTO.builder()
                .userId(profile.getUser().getId())
                .fullName(profile.getUser().getFullName())
                .vehicleMake(profile.getVehicleMake())
                .vehicleModel(profile.getVehicleModel())
                .vehiclePlate(profile.getVehiclePlate())
                .vehicleType(profile.getVehicleType().name())
                .verificationStatus(profile.getVerificationStatus().name())
                .build();
    }
    private VehicleType parseVehicleType(String value) {
        try {
            return VehicleType.valueOf(value.toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("Invalid vehicle type: " + value);
        }
    }

    @Override
    public void verifyDriver(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        DriverProfile profile = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        profile.setVerificationStatus(VerificationStatus.VERIFIED);
        user.setRole(Role.DRIVER);

        userRepository.save(user);
        driverRepository.save(profile);
    }

    @Override
    public void rejectDriver(UUID userId, String reason) {

        DriverProfile profile = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        profile.setVerificationStatus(VerificationStatus.REJECTED);
        profile.setRejectionReason(reason);

        driverRepository.save(profile);
    }

    @Override
    public List<DriverResponseDTO> getDrivers(VerificationStatus verificationStatus) {

        List<DriverProfile> drivers;

        if (verificationStatus != null) {
            drivers = driverRepository.findByVerificationStatus(verificationStatus);
        } else {
            drivers = driverRepository.findAll();
        }

        return drivers.stream()
                .map(this::mapToDTO)
                .toList();
    }
}
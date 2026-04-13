package com.raydo.raydoApplication.service;

import com.raydo.raydoApplication.dto.DriverApplyDTO;
import com.raydo.raydoApplication.dto.DriverResponseDTO;
import com.raydo.raydoApplication.entity.*;
import com.raydo.raydoApplication.repository.DriverRepository;
import com.raydo.raydoApplication.repository.UserRepository;
import com.raydo.raydoApplication.service.DriverServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DriverServiceImpl Tests")
class DriverServiceImplTest {

    @Mock private DriverRepository driverRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private DriverServiceImpl driverService;

    private UUID userId;
    private User sampleUser;
    private DriverProfile sampleProfile;
    private DriverApplyDTO applyDTO;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        sampleUser = User.builder()
                .id(userId)
                .fullName("Driver Dave")
                .phoneNumber("+919876543210")
                .role(Role.RIDER)
                .status(Status.ACTIVE)
                .build();

        sampleProfile = DriverProfile.builder()
                .user(sampleUser)
                .licenseNumber("TN-1234567890")
                .vehicleMake("Toyota")
                .vehicleModel("Innova")
                .vehicleYear(2022)
                .vehicleColor("White")
                .vehiclePlate("TN01AB1234")
                .vehicleType(VehicleType.ECONOMY)
                .verificationStatus(VerificationStatus.PENDING)
                .cityId(1)
                .build();

        applyDTO = new DriverApplyDTO();
        applyDTO.setLicenseNumber("TN-1234567890");
        applyDTO.setVehicleMake("Toyota");
        applyDTO.setVehicleModel("Innova");
        applyDTO.setVehicleYear(2022);
        applyDTO.setVehicleColor("White");
        applyDTO.setVehiclePlate("TN01AB1234");
        applyDTO.setVehicleType("ECONOMY");
        applyDTO.setCityId(1);
    }

    // ─────────────────────────────────────────────────────────────
    // applyForDriver
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("applyForDriver()")
    class ApplyForDriver {

        @Test
        @DisplayName("saves profile and returns DTO")
        void savesProfileAndReturnsDTO() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
            when(driverRepository.existsByUserId(userId)).thenReturn(false);
            when(driverRepository.save(any())).thenReturn(sampleProfile);

            DriverResponseDTO result = driverService.applyForDriver(userId, applyDTO);

            assertThat(result).isNotNull();
            assertThat(result.getVehicleMake()).isEqualTo("Toyota");
            assertThat(result.getVerificationStatus())
                    .isEqualTo(VerificationStatus.PENDING.name());

            verify(driverRepository).save(any());
        }

        @Test
        void throwsWhenUserNotFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> driverService.applyForDriver(userId, applyDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not found");
        }

        @Test
        void throwsWhenProfileExists() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
            when(driverRepository.existsByUserId(userId)).thenReturn(true);

            assertThatThrownBy(() -> driverService.applyForDriver(userId, applyDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("already exists");

            verify(driverRepository, never()).save(any());
        }

        @Test
        void throwsForInvalidVehicleType() {
            applyDTO.setVehicleType("FLYING_CARPET");

            when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
            when(driverRepository.existsByUserId(userId)).thenReturn(false);

            assertThatThrownBy(() -> driverService.applyForDriver(userId, applyDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid vehicle type");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // getDriverProfile
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getDriverProfile()")
    class GetDriverProfile {

        @Test
        void returnsDTO_whenFound() {
            when(driverRepository.findByUserId(userId)).thenReturn(Optional.of(sampleProfile));

            DriverResponseDTO result = driverService.getDriverProfile(userId);

            assertThat(result.getVehiclePlate()).isEqualTo("TN01AB1234");
            assertThat(result.getVehicleType())
                    .isEqualTo(VehicleType.ECONOMY.name());
        }

        @Test
        void throwsWhenNotFound() {
            when(driverRepository.findByUserId(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> driverService.getDriverProfile(userId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("not found");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // updateDriverProfile
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("updateDriverProfile()")
    class UpdateDriverProfile {

        @Test
        void updatesFields() {
            applyDTO.setVehicleColor("Black");
            applyDTO.setVehiclePlate("TN02XY9999");

            when(driverRepository.findByUserId(userId)).thenReturn(Optional.of(sampleProfile));
            when(driverRepository.save(any())).thenReturn(sampleProfile);

            DriverResponseDTO result = driverService.updateDriverProfile(userId, applyDTO);

            assertThat(result.getVehiclePlate()).isEqualTo("TN02XY9999");
        }

        @Test
        void throwsWhenNotFound() {
            when(driverRepository.findByUserId(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> driverService.updateDriverProfile(userId, applyDTO))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // verifyDriver
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("verifyDriver()")
    class VerifyDriver {

        @Test
        void setsVerifiedAndRole() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
            when(driverRepository.findByUserId(userId)).thenReturn(Optional.of(sampleProfile));

            driverService.verifyDriver(userId);

            assertThat(sampleProfile.getVerificationStatus())
                    .isEqualTo(VerificationStatus.VERIFIED);
            assertThat(sampleUser.getRole())
                    .isEqualTo(Role.DRIVER);

            verify(userRepository).save(sampleUser);
            verify(driverRepository).save(sampleProfile);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // rejectDriver
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("rejectDriver()")
    class RejectDriver {

        @Test
        void setsRejected() {
            when(driverRepository.findByUserId(userId)).thenReturn(Optional.of(sampleProfile));

            driverService.rejectDriver(userId, "Invalid documents");

            assertThat(sampleProfile.getVerificationStatus())
                    .isEqualTo(VerificationStatus.REJECTED);
            assertThat(sampleProfile.getRejectionReason())
                    .isEqualTo("Invalid documents");

            verify(driverRepository).save(sampleProfile);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // getDrivers
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getDrivers()")
    class GetDrivers {

        @Test
        void returnsFiltered() {
            when(driverRepository.findByVerificationStatus(VerificationStatus.PENDING))
                    .thenReturn(List.of(sampleProfile));

            List<DriverResponseDTO> result =
                    driverService.getDrivers(VerificationStatus.PENDING);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getVerificationStatus())
                    .isEqualTo(VerificationStatus.PENDING.name());
        }

        @Test
        void returnsAll_whenNull() {
            when(driverRepository.findAll())
                    .thenReturn(List.of(sampleProfile, sampleProfile));

            List<DriverResponseDTO> result =
                    driverService.getDrivers(null);

            assertThat(result).hasSize(2);
            verify(driverRepository).findAll();
        }
    }
}
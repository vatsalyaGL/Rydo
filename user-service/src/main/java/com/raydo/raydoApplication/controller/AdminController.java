package com.raydo.raydoApplication.controller;

import com.raydo.raydoApplication.Exceptions.UnauthorizedException;
import com.raydo.raydoApplication.dto.DriverResponseDTO;
import com.raydo.raydoApplication.dto.UserResponseDTO;
import com.raydo.raydoApplication.entity.Role;
import com.raydo.raydoApplication.entity.User;
import com.raydo.raydoApplication.entity.VerificationStatus;
import com.raydo.raydoApplication.repository.UserRepository;
import com.raydo.raydoApplication.service.DriverService;
import com.raydo.raydoApplication.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DriverService driverService;
    private final UserService userService;

    private void requireAdmin(UUID userId) {
        UserResponseDTO user = userService.getUserById(userId);
        if (user == null || user.getRole() != "ADMIN") {
            throw new UnauthorizedException("Admin access only");
        }
    }

    @PutMapping("/drivers/{id}/approve")
    public ResponseEntity<String> approveDriver(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID userId) {

        requireAdmin(userId);
        driverService.verifyDriver(id);
        return ResponseEntity.ok("Driver approved");
    }

    @PutMapping("/drivers/{id}/reject")
    public ResponseEntity<String> rejectDriver(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID userId,  @RequestParam String reason ) {

        requireAdmin(userId);
        driverService.rejectDriver(id, reason);
        return ResponseEntity.ok("Driver rejected");
    }

    @GetMapping("/drivers")
    public ResponseEntity<List<DriverResponseDTO>> getDrivers(
            @RequestParam VerificationStatus status,
            @RequestHeader("X-User-Id") UUID userId) {

        requireAdmin(userId);
        List<DriverResponseDTO> drivers = driverService.getDrivers(status);
        return ResponseEntity.ok(drivers);
    }
}
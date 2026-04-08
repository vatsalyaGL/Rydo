package com.raydo.raydoApplication.controller;

import com.raydo.raydoApplication.dto.DriverApplyDTO;
import com.raydo.raydoApplication.dto.DriverResponseDTO;
import com.raydo.raydoApplication.service.DriverService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/driver")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @PostMapping("/apply")
    public ResponseEntity<DriverResponseDTO> applyForDriver(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestBody DriverApplyDTO request
    ) {

        DriverResponseDTO response = driverService.applyForDriver(userId, request);

        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<DriverResponseDTO> getMyProfile(
            @RequestHeader("X-User-Id") UUID userId
    ) {

        DriverResponseDTO response = driverService.getDriverProfile(userId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/update")
    public ResponseEntity<DriverResponseDTO> updateDriverProfile(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestBody DriverApplyDTO request
    ) {

        DriverResponseDTO response = driverService.updateDriverProfile(userId, request);

        return ResponseEntity.ok(response);
    }
}
package com.rydo.location_service.controller;

import com.rydo.location_service.dto.LocationRequestDTO;
import com.rydo.location_service.dto.LocationUpdateDTO;
import com.rydo.location_service.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/location")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping("update")
    public ResponseEntity<Void> updateLocation(@Valid @RequestBody LocationUpdateDTO dto) {
        locationService.updateLocation(dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("driver-location")
    public ResponseEntity<LocationUpdateDTO> updateLocation(@Valid @RequestBody LocationRequestDTO dto) {
        LocationUpdateDTO loc = locationService.fetchDriverLocation(dto);
        return new ResponseEntity<>(loc, HttpStatus.FOUND);
    }
}
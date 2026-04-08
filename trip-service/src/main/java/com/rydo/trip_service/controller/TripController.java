package com.rydo.trip_service.controller;

import com.rydo.trip_service.dto.*;
import com.rydo.trip_service.service.TripService;
import com.rydo.trip_service.entity.Trip;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping
    public ResponseEntity<?> requestTrip(@Valid @RequestBody TripCreateRequest tripRequest) {
        Trip trip = tripService.createTrip(tripRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "trip_id", trip.getId(),
                "estimated_fare", trip.getEstimatedFare(),
                "surge_multiplier", trip.getSurgeMultiplier()
        ));
    }

    @PostMapping("/nearby")
    public ResponseEntity<List<RiderDTO>> getNearbyRiders(
            @Valid @RequestBody DriverDTO dto) {

        List<RiderDTO> riders = tripService.getNearbyRiders(dto);
        return ResponseEntity.ok(riders);
    }

    @PostMapping("accept-ride")
    public ResponseEntity<TripAcceptResponseDTO> acceptRide(
            @Valid @RequestBody TripAcceptDTO dto) {

        TripAcceptResponseDTO response = tripService.acceptRide(dto);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/status")
    public ResponseEntity<TripStatusResponseDTO> getTripStatus(@Valid @RequestBody TripStatusRequest dto) {

        TripStatusResponseDTO response = tripService.getTripStatus(dto);

        return ResponseEntity.ok(response);
    }
//    @PostMapping("details")
//    public ResponseEntity<TripDetails> getTripDetails(@Valid @RequestBody FetchTripDetails dto){
//        TripDetails d =tripService.getTripDetails(dto);
//        return new ResponseEntity<>(d, HttpStatus.FOUND);
//    }
}
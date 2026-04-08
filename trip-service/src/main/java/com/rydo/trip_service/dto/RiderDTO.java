package com.rydo.trip_service.dto;

import java.util.UUID;

import com.rydo.trip_service.enums.VehicleType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiderDTO {
    @NotNull
    private UUID tripId;
    @NotNull
    private UUID riderId;
    private String riderName;
    @NotNull
    private Double pickupLat;
    @NotNull
    private Double pickupLng;
    private String pickupAddress;
    @NotNull
    private Double dropoffLat;
    @NotNull
    private Double dropoffLng;
    private String dropoffAddress;
    private Double estimatedDistanceKm;
    private Double estimatedFare;
    private VehicleType vehicleType;
}
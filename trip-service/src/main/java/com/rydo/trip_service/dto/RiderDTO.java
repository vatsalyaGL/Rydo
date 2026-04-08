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
    private Double pickupLat;
    @NotNull
    private Double pickupLng;
     // e.g., "SEDAN", "BIKE"
    //private Double passengerRating;      Optional: to match with high-rated drivers
}
package com.rydo.trip_service.dto;

import com.rydo.trip_service.entity.Trip;
import com.rydo.trip_service.enums.RideType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import com.rydo.trip_service.enums.VehicleType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class TripCreateRequest {

    @NotNull(message = "Rider ID is required")
    private UUID riderId;
    
    @NotNull(message = "Pickup latitude is required")
    private Double pickupLat;

    @NotNull(message = "Pickup longitude is required")
    private Double pickupLng;

    @NotBlank(message = "Pickup address is required")
    private String pickupAddress;

    @NotNull(message = "Dropoff latitude is required")
    private Double dropoffLat;

    @NotNull(message = "Dropoff longitude is required")
    private Double dropoffLng;

    @NotBlank(message = "Dropoff address is required")
    private String dropoffAddress;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType; // e.g., ECONOMY, COMFORT

    @NotNull(message = "estimated Distance Km required")
    private BigDecimal estimatedDistanceKm;

    @NotNull(message = "autual Distance Km required")
    private BigDecimal actualDistanceKm;

    @NotNull(message = "estimated duration required")
    private Integer estimatedDurationSec;

    @NotNull(message = "actual duration required")
    private Integer actualDurationSec;

    @NotNull(message = "estimated fare required")
    private BigDecimal estimatedFare;

    @NotNull(message = "final fare required")
    private BigDecimal finalFare;

    @NotNull(message = "Ride type is required")
    private RideType rideType; // e.g., INSTANT, SCHEDULED

    @NotNull(message = "city cannot be null")
    private Integer cityId;

    // Optional: Only provided if the rider is scheduling a ride for later
    private OffsetDateTime scheduledFor;

    // Optional: If the rider applies a discount code on the checkout screen
    @Size(max = 20, message = "Promo code cannot exceed 20 characters")
    private String promoCode;
}
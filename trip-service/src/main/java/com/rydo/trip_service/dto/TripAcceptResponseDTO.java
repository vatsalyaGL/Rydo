package com.rydo.trip_service.dto;

import com.rydo.trip_service.enums.TripStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;
@Data
@AllArgsConstructor
public class TripAcceptResponseDTO {
    private UUID tripId;
    private UUID driverId;
    private UUID riderId;
    private TripStatus status;
    private int otp;
}
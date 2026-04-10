package com.rydo.trip_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TripStatusResponseDTO {

    private UUID tripId;
    private String status;

    private UUID DriverId;
}
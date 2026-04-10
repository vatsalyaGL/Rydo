package com.rydo.trip_service.dto;

import lombok.Data;

import java.util.UUID;
@Data
public class TripStatusRequest {
    private UUID tripId;
}

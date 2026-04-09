package com.rydo.trip_service.dto;

import com.rydo.trip_service.enums.TripStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class TripCompleteRequest {

    private UUID tripId;
    private String status;
}

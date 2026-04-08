package com.rydo.location_service.dto;

import lombok.Data;
import lombok.NonNull;

import java.util.UUID;
@Data
public class LocationRequestDTO {
    @NonNull
    private UUID driverId;
}

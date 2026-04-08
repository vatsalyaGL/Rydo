package com.rydo.location_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.util.UUID;
@Data
@AllArgsConstructor
public class LocationUpdateDTO {
//    String driverId, double lat, double lon
    @NonNull
    private UUID driverId;
    @NonNull
    private double latitude;
    @NonNull
    private double longitude;
}

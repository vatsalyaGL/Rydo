package com.raydo.raydoApplication.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class DriverResponseDTO {

    private UUID userId;
    private String fullName;

    private String vehicleMake;
    private String vehicleModel;
    private String vehiclePlate;

    private String vehicleType;
    private String verificationStatus;
}
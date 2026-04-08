package com.raydo.raydoApplication.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverApplyDTO {

    private String licenseNumber;
    private String vehicleMake;
    private String vehicleModel;
    private Integer vehicleYear;
    private String vehicleColor;
    private String vehiclePlate;
    private String vehicleType;
    private Integer cityId;
}
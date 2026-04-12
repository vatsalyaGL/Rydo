package com.raydo.raydoApplication.dto;

import com.raydo.raydoApplication.entity.VehicleType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VehicleTypeDTO {
    @NotNull
    public VehicleType vehicleType;
}

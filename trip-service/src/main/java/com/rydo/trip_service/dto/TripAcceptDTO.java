package com.rydo.trip_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class TripAcceptDTO {
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private UUID driverId;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private UUID tripId;
}
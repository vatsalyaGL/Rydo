package com.rydo.location_service.entity;

import lombok.Data;
import org.locationtech.jts.geom.Point;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "driver_locations")
public class DriverLocation {
    @Id
    @Column(name = "driver_id", columnDefinition = "uuid")
    private UUID driverId;

    // The 'columnDefinition' tells Postgres to use the Geometry type
    // SRID 4326 is the standard for GPS (WGS84)
    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point lastLocation;

    private LocalDateTime updatedAt;
}
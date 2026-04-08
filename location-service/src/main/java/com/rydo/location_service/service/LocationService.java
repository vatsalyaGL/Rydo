package com.rydo.location_service.service;
import com.rydo.location_service.dto.LocationRequestDTO;
import com.rydo.location_service.dto.LocationUpdateDTO;
import com.rydo.location_service.entity.DriverLocation;
import com.rydo.location_service.exception.DriverNotFoundException;
import com.rydo.location_service.repository.LocationRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class LocationService {
    private final LocationRepository repository;
    private final GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

    public LocationService(LocationRepository repository) {
        this.repository = repository;
    }

        public void updateLocation(LocationUpdateDTO dto) {
            DriverLocation loc = new DriverLocation();

            loc.setDriverId(dto.getDriverId());
            loc.setLastLocation(factory.createPoint(new Coordinate(dto.getLongitude(), dto.getLatitude())));
            loc.setUpdatedAt(LocalDateTime.now());
            repository.save(loc);
        }

        public LocationUpdateDTO fetchDriverLocation(LocationRequestDTO dto ){
            DriverLocation d = repository.findById(dto.getDriverId()).orElseThrow(() -> new DriverNotFoundException("Driver not found"));
            return  new LocationUpdateDTO(
                    dto.getDriverId(),
                    d.getLastLocation().getY(),
                    d.getLastLocation().getX()
            );
        }
}
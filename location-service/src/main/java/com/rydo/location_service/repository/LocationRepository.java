package com.rydo.location_service.repository;

import com.rydo.location_service.entity.DriverLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface LocationRepository extends JpaRepository<DriverLocation, UUID> {

    @Query(value = """
        SELECT driver_id FROM driver_locations 
        WHERE ST_DWithin(last_location, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326), :radiusInMeters)
        """, nativeQuery = true)
    List<String> findNearbyDrivers(double lat, double lon, double radiusInMeters);
}
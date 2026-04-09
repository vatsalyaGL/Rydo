package com.rydo.trip_service.repository;

import com.rydo.trip_service.entity.Trip;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TripRepository extends JpaRepository<Trip, UUID> {

    @Query(value = """
        SELECT * FROM trips 
        WHERE status = 'SEARCHING' AND vehicle_type = :vehicleType
        AND (6371 * acos(cos(radians(:driverLat)) * cos(radians(pickup_lat)) * cos(radians(pickup_lng) - radians(:driverLon)) + sin(radians(:driverLat)) * sin(radians(pickup_lat)))) <= :radiusInKm 
        ORDER BY (6371 * acos(cos(radians(:driverLat)) * cos(radians(pickup_lat)) * cos(radians(pickup_lng) - radians(:driverLon)) + sin(radians(:driverLat)) * sin(radians(pickup_lat)))) ASC
        """,
            nativeQuery = true)
    List<Trip> findNearbySearchingTrips(
            @Param("driverLat") double driverLat,
            @Param("driverLon") double driverLon,
            @Param("radiusInKm") double radiusInKm,
            @Param("vehicleType") String VehicleType,
            Pageable pageable
    );
    Optional<Trip> findById(UUID id);
}
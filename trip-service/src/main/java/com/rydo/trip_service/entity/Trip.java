package com.rydo.trip_service.entity;

import com.rydo.trip_service.enums.CancelledBy;
import com.rydo.trip_service.enums.RideType;
import com.rydo.trip_service.enums.TripStatus;
import com.rydo.trip_service.enums.VehicleType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "trips")
public class Trip {
    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "rider_id", nullable = false)
    private UUID riderId;

    @Column(name = "driver_id")
    private UUID driverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripStatus status;

    @Column(name = "pickup_lat", nullable = false)
    private Double pickupLat;

    @Column(name = "pickup_lng", nullable = false)
    private Double pickupLng;

    @Column(name = "pickup_address", columnDefinition = "TEXT")
    private String pickupAddress;

    @Column(name = "dropoff_lat", nullable = false)
    private Double dropoffLat;

    @Column(name = "dropoff_lng", nullable = false)
    private Double dropoffLng;

    @Column(name = "dropoff_address", columnDefinition = "TEXT")
    private String dropoffAddress;

    @Column(name = "route_polyline", columnDefinition = "TEXT")
    private String routePolyline;

    @Column(name = "estimated_distance_km", precision = 8, scale = 2)
    private BigDecimal estimatedDistanceKm;

    @Column(name = "actual_distance_km", precision = 8, scale = 2)
    private BigDecimal actualDistanceKm;

    @Column(name = "estimated_duration_sec")
    private Integer estimatedDurationSec;

    @Column(name = "actual_duration_sec")
    private Integer actualDurationSec;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false)
    private VehicleType vehicleType;

    @Column(name = "estimated_fare", precision = 10, scale = 2)
    private BigDecimal estimatedFare;

    @Column(name = "final_fare", precision = 10, scale = 2)
    private BigDecimal finalFare;

    @Column(name = "surge_multiplier", precision = 3, scale = 1)
    private BigDecimal surgeMultiplier = BigDecimal.valueOf(1.0);

    @Column(name = "currency_code", length = 3, nullable = false)
    private String currencyCode = "INR";

    @Column(name = "promo_code", length = 20)
    private String promoCode;

    @Column(name = "promo_discount", precision = 10, scale = 2)
    private BigDecimal promoDiscount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "ride_type", nullable = false)
    private RideType rideType;

    @Column(name = "scheduled_for")
    private OffsetDateTime scheduledFor;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancelled_by")
    private CancelledBy cancelledBy;

    @Column(name = "cancellation_fee", precision = 10, scale = 2)
    private BigDecimal cancellationFee = BigDecimal.ZERO;

    @Column(name = "city_id", nullable = false)
    private Integer cityId;

    @CreationTimestamp
    @Column(name = "requested_at", nullable = false, updatable = false)
    private OffsetDateTime requestedAt;

    @Column(name = "matched_at")
    private OffsetDateTime matchedAt;

    @Column(name = "driver_arrived_at")
    private OffsetDateTime driverArrivedAt;

    @Column(name = "pickup_at")
    private OffsetDateTime pickupAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;
}




package com.raydo.raydoApplication.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "driver_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverProfile {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = true)
    private String licenseNumber;

    @Column(nullable = false)
    private String vehicleMake;

    @Column(nullable = false)
    private String vehicleModel;

    @Column(nullable = false)
    private Integer vehicleYear;

    @Column(nullable = false)
    private String vehicleColor;

    @Column(unique = true)
    private String vehiclePlate;

    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;

    private Boolean isOnline = false;

    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus;

    private String rejectionReason;

    private Integer cityId;

    @Column(precision = 5, scale = 2)
    private BigDecimal acceptanceRate = BigDecimal.valueOf(100);

    @Column(precision = 5, scale = 2)
    private BigDecimal completionRate = BigDecimal.valueOf(100);
}
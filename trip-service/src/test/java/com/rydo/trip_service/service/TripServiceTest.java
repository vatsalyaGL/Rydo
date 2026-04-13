package com.rydo.trip_service.service;

import com.rydo.trip_service.dto.*;
import com.rydo.trip_service.entity.Trip;
import com.rydo.trip_service.enums.TripStatus;
import com.rydo.trip_service.enums.VehicleType;
import com.rydo.trip_service.repository.TripRepository;
import com.rydo.trip_service.service.TripService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private ModelMapper modelMapper;

    private TripService tripService;

    private UUID tripId;
    private Trip sampleTrip;

    @BeforeEach
    void setUp() {

        tripService = new TripService(tripRepository);

        tripId = UUID.randomUUID();

        sampleTrip = new Trip();
        sampleTrip.setId(tripId);
        sampleTrip.setEstimatedDistanceKm(new BigDecimal("5.0"));
        sampleTrip.setSurgeMultiplier(new BigDecimal("1.0"));
        sampleTrip.setStatus(TripStatus.SEARCHING);
        sampleTrip.setVehicleType(VehicleType.ECONOMY);
    }


    // ================= getNearbyRiders =================
    @Test
    void getNearbyRiders_success() {
        DriverDTO dto = new DriverDTO();
        dto.setDriverLat(13.0);
        dto.setDriverLon(80.0);
        dto.setVehicleType("ECONOMY");


        when(tripRepository.findNearbySearchingTrips(
                anyDouble(),
                anyDouble(),
                anyDouble(),
                anyString(),
                any(PageRequest.class)
        )).thenReturn(List.of(sampleTrip));

        List<RiderDTO> result = tripService.getNearbyRiders(dto);

        assertThat(result).hasSize(1);
    }

    // ================= acceptRide =================
    @Test
    void acceptRide_success() {
        UUID driverId = UUID.randomUUID();

        TripAcceptDTO dto = new TripAcceptDTO();
        dto.setTripId(tripId);
        dto.setDriverId(driverId);

        when(tripRepository.findById(tripId))
                .thenReturn(Optional.of(sampleTrip));

        when(tripRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        TripAcceptResponseDTO result = tripService.acceptRide(dto);

        assertThat(result.getStatus()).isEqualTo(TripStatus.DRIVER_ARRIVING);
        assertThat(result.getDriverId()).isEqualTo(driverId);
    }

    // ================= startRide =================
    @Test
    void startRide_success() {
        sampleTrip.setStatus(TripStatus.DRIVER_ARRIVING);

        TripAcceptDTO dto = new TripAcceptDTO();
        dto.setTripId(tripId);

        when(tripRepository.findById(tripId))
                .thenReturn(Optional.of(sampleTrip));

        tripService.startRide(dto);

        assertThat(sampleTrip.getStatus()).isEqualTo(TripStatus.IN_PROGRESS);
    }

    // ================= completeTrip =================
    @Test
    void completeTrip_success() {
        TripCompleteRequest req = new TripCompleteRequest();
        req.setTripId(tripId);

        when(tripRepository.findById(tripId))
                .thenReturn(Optional.of(sampleTrip));

        when(tripRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        tripService.completeTrip(req);

        assertThat(sampleTrip.getStatus()).isEqualTo(TripStatus.COMPLETED);
    }

    // ================= cancelTrip =================
    @Test
    void cancelTrip_success() {
        TripCancelRequest req = new TripCancelRequest();
        req.setTripId(tripId);

        when(tripRepository.findById(tripId))
                .thenReturn(Optional.of(sampleTrip));

        when(tripRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        Trip result = tripService.cancelTrip(req);

        assertThat(result.getStatus()).isEqualTo(TripStatus.CANCELLED);
    }

    // ================= getTripById =================
    @Test
    void getTripById_success() {
        when(tripRepository.findById(tripId))
                .thenReturn(Optional.of(sampleTrip));

        Trip result = tripService.getTripById(tripId);

        assertThat(result.getId()).isEqualTo(tripId);
    }

    @Test
    void getTripById_notFound() {
        when(tripRepository.findById(tripId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> tripService.getTripById(tripId))
                .isInstanceOf(RuntimeException.class);
    }
}
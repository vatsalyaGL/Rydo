package com.rydo.location_service;

import com.rydo.location_service.dto.LocationUpdateDTO;
import com.rydo.location_service.entity.DriverLocation;
import com.rydo.location_service.exception.DriverNotFoundException;
import com.rydo.location_service.repository.LocationRepository;
import com.rydo.location_service.service.LocationService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LocationService Tests")
class LocationServiceTest {

    @Mock
    private LocationRepository repository;

    @InjectMocks
    private LocationService locationService;

    private UUID driverId;

    @BeforeEach
    void setUp() {
        driverId = UUID.randomUUID();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateLocation
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("updateLocation()")
    class UpdateLocation {

        @Test
        @DisplayName("saves driver location without throwing")
        void savesLocation_successfully() {
            LocationUpdateDTO dto = new LocationUpdateDTO(driverId, 13.0827, 80.2707);
            when(repository.save(any(DriverLocation.class))).thenAnswer(inv -> inv.getArgument(0));

            assertThatCode(() -> locationService.updateLocation(dto))
                    .doesNotThrowAnyException();

            verify(repository).save(argThat(loc ->
                    loc.getDriverId().equals(driverId) &&
                            loc.getUpdatedAt() != null
            ));
        }

        @Test
        @DisplayName("stores longitude as X and latitude as Y")
        void storesLongitudeAsX_LatitudeAsY() {
            double lat = 13.0827;
            double lon = 80.2707;
            LocationUpdateDTO dto = new LocationUpdateDTO(driverId, lat, lon);

            when(repository.save(any(DriverLocation.class))).thenAnswer(inv -> inv.getArgument(0));

            locationService.updateLocation(dto);

            verify(repository).save(argThat(loc -> {
                Point p = loc.getLastLocation();
                return Double.compare(p.getX(), lon) == 0 &&
                        Double.compare(p.getY(), lat) == 0;
            }));
        }

        @Test
        @DisplayName("sets SRID 4326 on saved geometry")
        void setsSrid4326() {
            LocationUpdateDTO dto = new LocationUpdateDTO(driverId, 13.0, 80.0);
            when(repository.save(any(DriverLocation.class))).thenAnswer(inv -> inv.getArgument(0));

            locationService.updateLocation(dto);

            verify(repository).save(argThat(loc ->
                    loc.getLastLocation().getSRID() == 4326
            ));
        }

        @Test
        @DisplayName("updates updatedAt timestamp on every call")
        void updatesTimestamp() {
            LocationUpdateDTO dto = new LocationUpdateDTO(driverId, 13.0, 80.0);
            when(repository.save(any(DriverLocation.class))).thenAnswer(inv -> inv.getArgument(0));

            locationService.updateLocation(dto);

            verify(repository).save(argThat(loc ->
                    loc.getUpdatedAt() != null &&
                            loc.getUpdatedAt().isBefore(LocalDateTime.now().plusSeconds(1))
            ));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // fetchDriverLocation
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("fetchDriverLocation()")
    class FetchDriverLocation {

        private DriverLocation buildDriverLocation(double lat, double lon) {
            GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
            Point point = factory.createPoint(new Coordinate(lon, lat)); // JTS: (X=lon, Y=lat)

            DriverLocation loc = new DriverLocation();
            loc.setDriverId(driverId);
            loc.setLastLocation(point);
            loc.setUpdatedAt(LocalDateTime.now());
            return loc;
        }

        @Test
        @DisplayName("returns DTO with correct lat/lon for found driver")
        void returnsDTO_forFoundDriver() {
            DriverLocation loc = buildDriverLocation(13.0827, 80.2707);
            when(repository.findById(driverId)).thenReturn(Optional.of(loc));

            LocationUpdateDTO result = locationService.fetchDriverLocation(driverId);

            assertThat(result.getDriverId()).isEqualTo(driverId);
            assertThat(result.getLatitude()).isEqualTo(13.0827);
            assertThat(result.getLongitude()).isEqualTo(80.2707);
        }

        @Test
        @DisplayName("throws DriverNotFoundException when driver not found")
        void throwsDriverNotFoundException_whenNotFound() {
            when(repository.findById(driverId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> locationService.fetchDriverLocation(driverId))
                    .isInstanceOf(DriverNotFoundException.class)
                    .hasMessageContaining("Driver not found");
        }

        @Test
        @DisplayName("preserves precision of coordinates")
        void preservesPrecision() {
            double lat = 13.082740;
            double lon = 80.270718;
            DriverLocation loc = buildDriverLocation(lat, lon);
            when(repository.findById(driverId)).thenReturn(Optional.of(loc));

            LocationUpdateDTO result = locationService.fetchDriverLocation(driverId);

            assertThat(result.getLatitude()).isEqualTo(lat);
            assertThat(result.getLongitude()).isEqualTo(lon);
        }
    }
}
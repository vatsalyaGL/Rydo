import { Component, AfterViewInit, NgZone, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { API_URLS } from '../../core/api.config';

let L: any;

interface NearbyRider {
  tripId: string;
  riderId: string;
  riderName?: string;
  pickupLat: number;
  pickupLng: number;
  pickupAddress: string;
  dropoffLat: number;
  dropoffLng: number;
  dropoffAddress: string;
  estimatedDistanceKm: number;
  estimatedFare: number;
  vehicleType: string;
}

type ViewState = 'idle' | 'loading' | 'list' | 'accepted' | 'in-ride';

@Component({
  selector: 'app-driver-rides',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './driver-rides.component.html',
  styleUrl: './driver-rides.component.css'
})
export class DriverRidesComponent implements AfterViewInit, OnDestroy {
  // State
  viewState: ViewState = 'idle';
  nearbyRiders: NearbyRider[] = [];
  acceptedRide: NearbyRider | null = null;
  errorMsg: string = '';
  isFetching: boolean = false;

  // Driver location
  driverLat: number = 0;
  driverLng: number = 0;

  // Location update polling
  locationIntervalId: any = null;
  isSendingLocation: boolean = false;
  locationStatus: 'inactive' | 'active' | 'stopped' = 'inactive';

  // Route animation
  routeCoords: number[][] = [];
  driverMarker: any = null;
  animationIndex: number = 0;
  animationInterval: any = null;
  driverReachedDestination: boolean = false;

  // Map
  map: any;

  constructor(
    private zone: NgZone,
    private cdr: ChangeDetectorRef,
    private http: HttpClient,
    private auth: AuthService,
    private router: Router
  ) {}

  async ngAfterViewInit() {
    if (typeof window !== 'undefined') {
      const leaflet = await import('leaflet');
      L = leaflet;
      this.getDriverLocation();
    }
  }

  ngOnDestroy() {
    this.stopLocationUpdates();
    this.stopDriverAnimation();
  }

  getDriverLocation(): Promise<boolean> {
    return new Promise((resolve) => {
      if (!navigator.geolocation) {
        resolve(false);
        return;
      }
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          this.zone.run(() => {
            this.driverLat = pos.coords.latitude;
            this.driverLng = pos.coords.longitude;
            resolve(true);
          });
        },
        (err) => {
          console.warn('Geolocation error:', err.message);
          resolve(false);
        }
      );
    });
  }

  async getNearbyRiders() {
    this.isFetching = true;

    const userId = this.auth.getUserId();
    if (!userId) {
      this.errorMsg = 'User not logged in';
      this.isFetching = false;
      return;
    }

    if (this.driverLat === 0 && this.driverLng === 0) {
      await this.getDriverLocation();
    }

    const vehicleType = this.auth.getVehicleType() || 'ECONOMY';

    const payload = {
      driverId: userId,
      driverLat: this.driverLat,
      driverLon: this.driverLng,
      vehicleType: vehicleType,
    };

    this.http.post<any>(
      API_URLS.TRIPS_NEARBY,
      payload
    ).subscribe({
      next: (res) => {
        this.nearbyRiders = Array.isArray(res) ? res : [];
        this.viewState = 'list';
        this.isFetching = false;
      },
      error: () => {
        this.errorMsg = 'Failed to fetch nearby riders';
        this.isFetching = false;
      }
    });
  }

  async acceptRide(rider: NearbyRider) {
    const userId = this.auth.getUserId();
    if (!userId) {
      this.errorMsg = 'User not logged in';
      return;
    }

    if (this.driverLat === 0 && this.driverLng === 0) {
      this.getDriverLocation();
    }

    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      ...(userId ? { 'X-User-Id': userId } : {})
    });

    const payload = {
      driverId: userId,
      tripId: rider.tripId
    };

    console.log('Accepting ride with payload:', payload);
    this.http.post<any>(
      API_URLS.TRIPS_ACCEPT,
      payload,
      { headers }
    ).subscribe({
      next: (res) => {
        console.log('Accept ride success:', res);
        this.zone.run(() => {
          this.acceptedRide = rider;
          this.viewState = 'accepted';
          this.cdr.detectChanges();
          setTimeout(() => this.initAcceptedMap(rider), 300);

          // Send initial location update
          const locationPayload = {
            driverId: userId,
            latitude: this.driverLat,
            longitude: this.driverLng
          };
          this.http.post<any>(
            API_URLS.LOCATION_UPDATE,
            locationPayload,
            { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) }
          ).subscribe({
            next: () => console.log('Initial location update sent'),
            error: (err) => console.warn('Initial location update failed:', err)
          });
        });
      },
      error: (err) => {
        console.log('Accept ride error:', err);
        this.zone.run(() => {
          this.errorMsg = err?.error?.message ?? 'Failed to accept ride.';
          this.cdr.detectChanges();
        });
      }
    });
  }

  rejectRide(rider: NearbyRider) {
    this.nearbyRiders = this.nearbyRiders.filter(r => r.tripId !== rider.tripId);
    if (this.nearbyRiders.length === 0) {
      this.viewState = 'idle';
    }
    this.cdr.detectChanges();
  }

  startRide() {
    if (!this.acceptedRide || this.routeCoords.length === 0) return;

    const userId = this.auth.getUserId();
    if (!userId) {
      this.errorMsg = 'User not logged in';
      return;
    }

    const payload = {
      driverId: userId,
      tripId: this.acceptedRide.tripId
    };

    this.http.post<any>(
      API_URLS.TRIPS_START,
      payload
    ).subscribe({
      next: (res) => {
        console.log('Start ride success:', res);
        this.zone.run(() => {
          this.viewState = 'in-ride';
          this.startDriverAnimation();
          setTimeout(() => this.initRideMap(this.acceptedRide!), 300);
          this.cdr.detectChanges();
        });
      },
      error: (err) => {
        console.log('Start ride error:', err);
        this.zone.run(() => {
          this.errorMsg = err?.error?.message ?? 'Failed to start ride.';
          this.cdr.detectChanges();
        });
      }
    });
  }

  completeTrip() {
    if (!this.acceptedRide) return;

    const userId = this.auth.getUserId();
    if (!userId) {
      this.errorMsg = 'User not logged in';
      return;
    }

    const tripId = this.acceptedRide.tripId;
    const payload = {
      tripId: tripId,
      driverId: userId
    };

    this.http.put<any>(
      API_URLS.TRIPS_COMPLETE,
      payload
    ).subscribe({
      next: (res) => {
        console.log('Trip completed successfully:', res);
        this.stopLocationUpdates();
        this.stopDriverAnimation();
        this.router.navigate(['/payment', tripId], {
          state: {
            riderId: this.acceptedRide?.riderId,
            amount: this.acceptedRide?.estimatedFare
          }
        });
      },
      error: (err) => {
        console.error('Error completing trip:', err);
        this.zone.run(() => {
          this.errorMsg = err?.error?.message ?? 'Failed to complete trip.';
          this.cdr.detectChanges();
        });
      }
    });
  }

  isRideInProgress(): boolean {
    return this.viewState === 'in-ride';
  }

  isRideAccepted(): boolean {
    return this.viewState === 'accepted';
  }

  initAcceptedMap(rider: NearbyRider) {
    if (!L) return;
    const mapEl = document.getElementById('driver-accepted-map');
    if (!mapEl) return;

    const map = L.map('driver-accepted-map').setView([rider.pickupLat, rider.pickupLng], 13);
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(map);

    if (this.driverLat && this.driverLng) {
      this.driverMarker = L.marker([this.driverLat, this.driverLng], { icon: this.createCarIcon() })
        .bindPopup('🚗 You').addTo(map);
    }
    L.marker([rider.pickupLat, rider.pickupLng], { icon: this.createDotIcon('green') })
      .bindPopup('📍 Pickup').addTo(map);
    L.marker([rider.dropoffLat, rider.dropoffLng], { icon: this.createDotIcon('red') })
      .bindPopup('🏁 Dropoff').addTo(map);

    const routePoints = [];
    if (this.driverLat && this.driverLng) {
      routePoints.push([this.driverLng, this.driverLat]);
    }
    routePoints.push([rider.pickupLng, rider.pickupLat]);
    routePoints.push([rider.dropoffLng, rider.dropoffLat]);

    if (routePoints.length >= 2) {
      const routeUrl = `https://router.project-osrm.org/route/v1/driving/${routePoints.map(p => p.join(',')).join(';')}?overview=full&geometries=geojson`;
      fetch(routeUrl)
        .then(res => res.json())
        .then((data) => {
          if (data.code === 'Ok' && data.routes?.length) {
            this.routeCoords = data.routes[0].geometry.coordinates.map((c: number[]) => [c[1], c[0]]);
            L.polyline(this.routeCoords, { color: '#2563eb', weight: 5, opacity: 0.8 }).addTo(map);
          } else {
            console.warn('Routing failed:', data);
          }
        })
        .catch((err) => {
          console.warn('Routing request failed:', err);
        });
    }

    const bounds = L.latLngBounds([
      [rider.pickupLat, rider.pickupLng],
      [rider.dropoffLat, rider.dropoffLng]
    ]);
    if (this.driverLat) bounds.extend([this.driverLat, this.driverLng]);
    map.fitBounds(bounds, { padding: [50, 50] });
  }

  initRideMap(rider: NearbyRider) {
    if (!L) return;
    const mapEl = document.getElementById('driver-accepted-map');
    if (!mapEl) return;

    const map = L.map('driver-accepted-map').setView([rider.pickupLat, rider.pickupLng], 13);
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(map);

    if (this.driverLat && this.driverLng) {
      this.driverMarker = L.marker([this.driverLat, this.driverLng], { icon: this.createCarIcon() })
        .bindPopup('🚗 You').addTo(map);
    }
    L.marker([rider.pickupLat, rider.pickupLng], { icon: this.createDotIcon('green') })
      .bindPopup('📍 Pickup').addTo(map);
    L.marker([rider.dropoffLat, rider.dropoffLng], { icon: this.createDotIcon('red') })
      .bindPopup('🏁 Dropoff').addTo(map);

    if (this.routeCoords.length > 0) {
      L.polyline(this.routeCoords, { color: '#2563eb', weight: 5, opacity: 0.8 }).addTo(map);
    }

    const bounds = L.latLngBounds([
      [rider.pickupLat, rider.pickupLng],
      [rider.dropoffLat, rider.dropoffLng]
    ]);
    if (this.driverLat) bounds.extend([this.driverLat, this.driverLng]);
    map.fitBounds(bounds, { padding: [50, 50] });
  }

  cancelCurrentRide() {
    if (!this.acceptedRide) {
      this.backToIdle();
      return;
    }

    const userId = this.auth.getUserId();
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      ...(userId ? { 'X-User-Id': userId } : {})
    });

    const payload = { tripId: this.acceptedRide.tripId };

    this.stopLocationUpdates();
    this.stopDriverAnimation();

    this.http.post<any>(
      API_URLS.TRIPS_CANCEL_RIDE,
      payload,
      { headers }
    ).subscribe({
      next: () => {
        console.log('Ride canceled successfully');
        this.zone.run(() => {
          this.backToIdle();
        });
      },
      error: (err) => {
        console.error('Cancel ride error:', err);
        this.zone.run(() => {
          this.errorMsg = err?.error?.message ?? 'Unable to cancel ride.';
          this.backToIdle();
        });
      }
    });
  }

  backToIdle() {
    this.viewState = 'idle';
    this.acceptedRide = null;
    this.nearbyRiders = [];
    this.errorMsg = '';
    this.driverReachedDestination = false;
    this.stopLocationUpdates();
    this.stopDriverAnimation();
  }

  stopLocationUpdates() {
    if (this.locationIntervalId) {
      clearInterval(this.locationIntervalId);
      this.locationIntervalId = null;
    }
    this.isSendingLocation = false;
    this.locationStatus = this.viewState === 'in-ride' ? 'stopped' : 'inactive';
  }

  private startDriverAnimation() {
    if (this.routeCoords.length === 0) return;

    this.animationIndex = 0;
    this.locationStatus = 'active';
    this.driverReachedDestination = false;

    this.sendLocationUpdate();

    let locationUpdateCounter = 0;

    this.animationInterval = setInterval(() => {
      if (this.animationIndex >= this.routeCoords.length - 1) {
        this.zone.run(() => {
          this.driverReachedDestination = true;
          this.cdr.detectChanges();
        });
        this.stopDriverAnimation();
        return;
      }

      this.animationIndex++;
      const currentCoord = this.routeCoords[this.animationIndex];

      if (this.driverMarker) {
        this.driverMarker.setLatLng(currentCoord);
      }

      this.driverLat = currentCoord[0];
      this.driverLng = currentCoord[1];

      locationUpdateCounter++;
      if (locationUpdateCounter >= 10) {
        this.sendLocationUpdate();
        locationUpdateCounter = 0;
      }
    }, 200);
  }

  private stopDriverAnimation() {
    if (this.animationInterval) {
      clearInterval(this.animationInterval);
      this.animationInterval = null;
    }
    this.locationStatus = 'stopped';
    this.animationIndex = 0;
  }

  private sendLocationUpdate() {
    if (!this.acceptedRide) return;

    const userId = this.auth.getUserId();
    if (!userId) return;

    const payload = {
      driverId: userId,
      latitude: this.driverLat,
      longitude: this.driverLng
    };

    this.http.post<any>(
      API_URLS.LOCATION_UPDATE,
      payload,
      { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) }
    ).subscribe({
      next: () => {
        console.log('Location update sent:', payload);
      },
      error: (err) => {
        console.warn('Location update failed:', err);
      }
    });
  }

  createCarIcon() {
    const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="36" height="36" viewBox="0 0 36 36">
      <circle cx="18" cy="18" r="17" fill="#2563eb" stroke="white" stroke-width="2"/>
      <text x="18" y="23" text-anchor="middle" font-size="16">🚗</text>
    </svg>`;
    return L.divIcon({ html: svg, iconSize: [36, 36], iconAnchor: [18, 18], className: '' });
  }

  createDotIcon(color: string) {
    const hex = color === 'green' ? '#16a34a' : '#dc2626';
    const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="28" height="40" viewBox="0 0 28 40">
      <path d="M14 0C6.268 0 0 6.268 0 14c0 9.333 14 26 14 26S28 23.333 28 14C28 6.268 21.732 0 14 0z"
        fill="${hex}" stroke="white" stroke-width="1.5"/>
      <circle cx="14" cy="14" r="5" fill="white"/>
    </svg>`;
    return L.divIcon({ html: svg, iconSize: [28, 40], iconAnchor: [14, 40], className: '' });
  }

  getVehicleIcon(type: string): string {
    const icons: Record<string, string> = {
      ECONOMY: '🚗', COMFORT: '🚙', XL: '🚐', MOTO: '🏍️', AUTO: '🛺'
    };
    return icons[type] ?? '🚗';
  }
}
import { Component, AfterViewInit, NgZone, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';

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

@Component({
  selector: 'app-driver-rides',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './driver-rides.component.html',
  styleUrl: './driver-rides.component.css'
})
export class DriverRidesComponent implements AfterViewInit, OnDestroy {
  // State
  viewState: 'idle' | 'loading' | 'list' | 'accepted' = 'idle';
  nearbyRiders: NearbyRider[] = [];
  acceptedRide: NearbyRider | null = null;
  errorMsg: string = '';
  isFetching: boolean = false;

  // Driver location
  driverLat: number = 0;
  driverLng: number = 0;

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

  ngOnDestroy() {}

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

    // Ensure location is fetched
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
      `http://localhost:8082/api/trips/nearby`,
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

    // Start fetching location for map
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
      `http://localhost:8082/api/trips/accept-ride`,
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
    // Remove from list locally; optionally notify backend
    this.nearbyRiders = this.nearbyRiders.filter(r => r.tripId !== rider.tripId);
    if (this.nearbyRiders.length === 0) {
      this.viewState = 'idle';
    }
    this.cdr.detectChanges();
  }

  initAcceptedMap(rider: NearbyRider) {
    if (!L) return;
    const mapEl = document.getElementById('driver-accepted-map');
    if (!mapEl) return;

    const map = L.map('driver-accepted-map').setView([rider.pickupLat, rider.pickupLng], 13);
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(map);

    // Driver position
    if (this.driverLat && this.driverLng) {
      L.marker([this.driverLat, this.driverLng], { icon: this.createCarIcon() })
        .bindPopup('🚗 You').addTo(map);
    }
    // Pickup
    L.marker([rider.pickupLat, rider.pickupLng], { icon: this.createDotIcon('green') })
      .bindPopup('📍 Pickup').addTo(map);
    // Dropoff
    L.marker([rider.dropoffLat, rider.dropoffLng], { icon: this.createDotIcon('red') })
      .bindPopup('🏁 Dropoff').addTo(map);

    const bounds = L.latLngBounds([
      [rider.pickupLat, rider.pickupLng],
      [rider.dropoffLat, rider.dropoffLng]
    ]);
    if (this.driverLat) bounds.extend([this.driverLat, this.driverLng]);
    map.fitBounds(bounds, { padding: [50, 50] });
  }

  backToIdle() {
    this.viewState = 'idle';
    this.acceptedRide = null;
    this.nearbyRiders = [];
    this.errorMsg = '';
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

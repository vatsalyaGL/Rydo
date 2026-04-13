import { Component, AfterViewInit, NgZone, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { API_URLS } from '../../core/api.config';

let L: any;

type ViewState = 'booking' | 'searching' | 'tracking';

@Component({
  selector: 'app-forms',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './forms.component.html',
  styleUrl: './forms.component.css'
})
export class FormsComponent implements AfterViewInit, OnDestroy {

  // ── Map & Route ─────────────────────────────────────────────────────────
  map: any;
  trackingMap: any;
  sourceMarker: any;
  destMarker: any;
  driverMarker: any;
  routePolyline: any;

  // ── Location inputs ─────────────────────────────────────────────────────
  sourceAddress: string = '';
  destAddress: string = '';
  destSearchQuery: string = '';
  suggestions: any[] = [];
  showSuggestions: boolean = false;
  isCalculating: boolean = false;
  isBooking: boolean = false;

  // ── State machine ────────────────────────────────────────────────────────
  viewState: ViewState = 'booking';
  bookingError: string = '';
  bookedTrip: any = null;

  // ── Searching/loader ─────────────────────────────────────────────────────
  searchSecondsLeft: number = 120;
  searchTimerRef: any = null;
  pollIntervalRef: any = null;
  noDriverFound: boolean = false;

  // ── Trip & driver tracking ───────────────────────────────────────────────
  acceptedTrip: any = null;
  driverLat: number = 0;
  driverLng: number = 0;
  trackingPollRef: any = null;
  completionPollRef: any = null;
  driverArriving: boolean = false;
  driverArrivingTimeoutRef: any = null;

  // ── Fare/distance ────────────────────────────────────────────────────────
  distance: number = 0;
  duration: number = 0;
  fare: number = 0;
  carType: string = 'mini';

  sourceLat: number = 0;
  sourceLng: number = 0;
  destLat: number = 0;
  destLng: number = 0;

  fareRates: { [key: string]: { rate: number; label: string; icon: string; base: number; vehicleType: string } } = {
    mini:  { rate: 10, label: 'Economy', icon: '🚗',  base: 30, vehicleType: 'ECONOMY' },
    sedan: { rate: 15, label: 'Comfort', icon: '🚙',  base: 50, vehicleType: 'COMFORT' },
    suv:   { rate: 20, label: 'XL',      icon: '🚐',  base: 80, vehicleType: 'XL'      },
    moto:  { rate: 6,  label: 'Moto',    icon: '🏍️', base: 20, vehicleType: 'MOTO'    },
    auto:  { rate: 8,  label: 'Auto',    icon: '🛺',  base: 25, vehicleType: 'AUTO'    }
  };

  private searchTimeout: any;

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
      this.initMap();
      this.getCurrentLocation();
    }
  }

  ngOnDestroy() {
    this.clearTimers();
  }

  private clearTimers() {
    if (this.searchTimerRef) {
      clearInterval(this.searchTimerRef);
      this.searchTimerRef = null;
    }
    if (this.pollIntervalRef) {
      clearInterval(this.pollIntervalRef);
      this.pollIntervalRef = null;
    }
    if (this.trackingPollRef) {
      clearInterval(this.trackingPollRef);
      this.trackingPollRef = null;
    }
    if (this.completionPollRef) {
      clearInterval(this.completionPollRef);
      this.completionPollRef = null;
    }
    if (this.driverArrivingTimeoutRef) {
      clearTimeout(this.driverArrivingTimeoutRef);
      this.driverArrivingTimeoutRef = null;
    }
  }

  // ── Map init ─────────────────────────────────────────────────────────────
  initMap() {
    this.map = L.map('map').setView([13.0827, 80.2707], 13);
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);
  }

  private destroyTrackingMap() {
    if (this.trackingMap) {
      this.trackingMap.remove();
      this.trackingMap = null;
      this.driverMarker = null;
    }
  }

  initTrackingMap() {
    setTimeout(() => {
      this.destroyTrackingMap();

      const pickupLat = this.sourceLat || this.acceptedTrip?.pickupLat || 13.0827;
      const pickupLng = this.sourceLng || this.acceptedTrip?.pickupLng || 80.2707;
      const dropoffLat = this.destLat || this.acceptedTrip?.dropoffLat || pickupLat;
      const dropoffLng = this.destLng || this.acceptedTrip?.dropoffLng || pickupLng;

      this.trackingMap = L.map('tracking-map').setView([pickupLat, pickupLng], 14);
      L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors'
      }).addTo(this.trackingMap);

      L.marker([pickupLat, pickupLng], { icon: this.createIcon('green') })
        .bindPopup('📍 Pickup').addTo(this.trackingMap);
      L.marker([dropoffLat, dropoffLng], { icon: this.createIcon('red') })
        .bindPopup('🏁 Destination').addTo(this.trackingMap);

      // Fetch driver location from API
      const driverId = this.acceptedTrip?.driverId ?? this.acceptedTrip?.DriverId;
      const userId = this.auth.getUserId();
      const headers = new HttpHeaders(userId ? { 'X-User-Id': userId } : {});

      this.http.get<any>(API_URLS.LOCATION_DRIVER(driverId), { headers })
        .subscribe({
          next: (response) => {
            this.zone.run(() => {
              const data = response?.data ?? response;
              if (data.latitude && data.longitude) {
                this.driverLat = data.latitude;
                this.driverLng = data.longitude;
                this.driverMarker = L.marker([this.driverLat, this.driverLng], { icon: this.createCarIcon() })
                  .bindPopup('🚗 Driver').addTo(this.trackingMap);

                // Draw route from driver position to destination
                this.drawRouteToDestination();

                const bounds = L.latLngBounds([
                  [pickupLat, pickupLng],
                  [dropoffLat, dropoffLng],
                  [this.driverLat, this.driverLng]
                ]);
                this.trackingMap.fitBounds(bounds, { padding: [50, 50] });
                this.trackingMap.invalidateSize();
              }
            });
          },
          error: (err) => {
            console.error('Error fetching initial driver location:', err);
            // Fallback: place marker at pickup location
            this.driverLat = pickupLat;
            this.driverLng = pickupLng;
            this.driverMarker = L.marker([this.driverLat, this.driverLng], { icon: this.createCarIcon() })
              .bindPopup('🚗 Driver').addTo(this.trackingMap);

            const bounds = L.latLngBounds([
              [pickupLat, pickupLng],
              [dropoffLat, dropoffLng],
              [this.driverLat, this.driverLng]
            ]);
            this.trackingMap.fitBounds(bounds, { padding: [50, 50] });
            this.trackingMap.invalidateSize();
          }
        });
    }, 300);
  }

  getCurrentLocation() {
    if (!navigator.geolocation) return;
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        this.zone.run(() => {
          this.sourceLat = pos.coords.latitude;
          this.sourceLng = pos.coords.longitude;
          if (this.sourceMarker) this.map.removeLayer(this.sourceMarker);
          this.sourceMarker = L.marker([this.sourceLat, this.sourceLng], {
            icon: this.createIcon('green')
          }).bindPopup('📍 Your Location').addTo(this.map);
          this.map.setView([this.sourceLat, this.sourceLng], 14);
          this.getAddress(this.sourceLat, this.sourceLng, 'source');
        });
      },
      (err) => console.warn('Geolocation error:', err.message)
    );
  }

  async getAddress(lat: number, lng: number, type: string) {
    try {
      const res = await fetch(
        `https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${lat}&lon=${lng}`
      );
      const data = await res.json();
      this.zone.run(() => {
        if (type === 'source') {
          this.sourceAddress = data.display_name;
        } else {
          this.destAddress = data.display_name;
          this.destSearchQuery = data.display_name;
        }
      });
    } catch (e) {
      console.error('Reverse geocode failed', e);
    }
  }

  onDestInput() {
    clearTimeout(this.searchTimeout);
    this.destAddress = '';
    this.distance = 0;
    this.fare = 0;
    this.bookingError = '';
    if (this.destSearchQuery.length < 3) {
      this.suggestions = [];
      this.showSuggestions = false;
      return;
    }
    this.searchTimeout = setTimeout(() => this.fetchSuggestions(), 400);
  }

  async fetchSuggestions() {
    try {
      const res = await fetch(
        `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(this.destSearchQuery)}&limit=6&addressdetails=1`
      );
      const data = await res.json();
      this.zone.run(() => {
        this.suggestions = data;
        this.showSuggestions = data.length > 0;
      });
    } catch (e) {
      console.error('Geocode search failed', e);
    }
  }

  selectSuggestion(suggestion: any) {
    this.zone.run(() => {
      this.destLat = parseFloat(suggestion.lat);
      this.destLng = parseFloat(suggestion.lon);
      this.destAddress = suggestion.display_name;
      this.destSearchQuery = suggestion.display_name;
      this.showSuggestions = false;
      this.suggestions = [];

      if (this.destMarker) this.map.removeLayer(this.destMarker);
      this.destMarker = L.marker([this.destLat, this.destLng], {
        icon: this.createIcon('red')
      }).bindPopup('🏁 Destination').addTo(this.map);

      this.isCalculating = true;
      this.calculateRoute();
    });
  }

  hideSuggestions() {
    setTimeout(() => {
      this.zone.run(() => { this.showSuggestions = false; });
    }, 200);
  }

  calculateRoute() {
    if (!this.sourceLat || !this.destLat) return;

    if (this.routePolyline) {
      this.map.removeLayer(this.routePolyline);
      this.routePolyline = null;
    }

    const url = `https://router.project-osrm.org/route/v1/driving/${this.sourceLng},${this.sourceLat};${this.destLng},${this.destLat}?overview=full&geometries=geojson`;
    fetch(url)
      .then(res => res.json())
      .then(data => {
        this.zone.run(() => {
          if (data.code !== 'Ok' || !data.routes?.length) {
            this.isCalculating = false;
            this.bookingError = 'Could not find a route. Try a different destination.';
            this.cdr.detectChanges();
            return;
          }

          const route = data.routes[0];
          this.distance = parseFloat((route.distance / 1000).toFixed(2));
          this.duration = Math.ceil(route.duration / 60);
          this.isCalculating = false;

          const coords = route.geometry.coordinates.map((c: number[]) => [c[1], c[0]]);
          this.routePolyline = L.polyline(coords, {
            color: '#2563eb', weight: 5, opacity: 0.85
          }).addTo(this.map);

          this.map.fitBounds(this.routePolyline.getBounds(), { padding: [50, 50] });
          this.calculateFare();
          this.cdr.detectChanges();
        });
      })
      .catch(() => {
        this.zone.run(() => {
          this.isCalculating = false;
          this.bookingError = 'Routing service unavailable. Check your internet.';
          this.cdr.detectChanges();
        });
      });
  }

  calculateFare() {
    if (this.distance === 0) return;
    const config = this.fareRates[this.carType];
    this.fare = parseFloat((config.base + this.distance * config.rate).toFixed(2));
  }

  onCarTypeChange() {
    if (this.distance > 0) this.calculateFare();
  }

  getFareForType(type: string): number {
    if (this.distance === 0) return 0;
    const config = this.fareRates[type];
    return parseFloat((config.base + this.distance * config.rate).toFixed(2));
  }

  get searchProgress(): number {
    return Math.round(((120 - this.searchSecondsLeft) / 120) * 100);
  }

  private buildPayload() {
    const riderId = this.auth.getUserId();
    return {
      riderId,
      pickupLat:            this.sourceLat,
      pickupLng:            this.sourceLng,
      pickupAddress:        this.sourceAddress,
      dropoffLat:           this.destLat,
      dropoffLng:           this.destLng,
      dropoffAddress:       this.destAddress,
      vehicleType:          this.fareRates[this.carType].vehicleType,
      estimatedDistanceKm:  this.distance,
      actualDistanceKm:     this.distance,
      estimatedDurationSec: this.duration * 60,
      actualDurationSec:    this.duration * 60,
      estimatedFare:        this.fare,
      finalFare:            this.fare,
      rideType:             'INSTANT',
      cityId:               1
    };
  }

  // ── Book Ride ─────────────────────────────────────────────────────────
  bookRide() {
    console.log('📍 bookRide() called');
    if (!this.auth.getUserId()) {
      this.bookingError = 'You must be logged in to book a ride.';
      return;
    }
    if (this.distance === 0 || !this.destAddress) {
      this.bookingError = 'Please select a destination first.';
      return;
    }

    this.bookingError = '';
    this.isBooking = true;

    const payload = this.buildPayload();
    const userId = this.auth.getUserId();
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      ...(userId ? { 'X-User-Id': userId } : {})
    });

    this.http.post(API_URLS.TRIPS_BASE, payload, { headers })
      .subscribe({
        next: (response: any) => {
          console.log('✅ Booking success:', response);
          this.zone.run(() => {
            this.isBooking = false;
            this.bookedTrip = response?.data ?? response;
            this.startSearchingState();
            this.cdr.detectChanges();
          });
        },
        error: (err: any) => {
          console.error('❌ Booking error:', err);
          this.zone.run(() => {
            this.isBooking = false;
            this.bookingError = err?.error?.message ?? err?.error?.error ?? 'Booking failed. Please try again.';
            this.cdr.detectChanges();
          });
        }
      });
  }

  // ── Searching State ───────────────────────────────────────────────────
  startSearchingState() {
    this.viewState = 'searching';
    this.searchSecondsLeft = 120;
    this.noDriverFound = false;

    const tripId = this.bookedTrip?.id ?? this.bookedTrip?.tripId ?? this.bookedTrip?.trip_id;
    if (!tripId) {
      console.error('❌ No tripId found! Cannot start polling');
      return;
    }

    this.searchTimerRef = setInterval(() => {
      this.zone.run(() => {
        this.searchSecondsLeft--;
        this.checkTripStatus(tripId);

        if (this.searchSecondsLeft <= 0) {
          this.clearTimers();
          this.noDriverFound = true;
          this.cdr.detectChanges();
        }
      });
    }, 2000);
  }

  private checkTripStatus(tripId: string) {
    const userId = this.auth.getUserId();
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      ...(userId ? { 'X-User-Id': userId } : {})
    });

    this.http.post<any>(API_URLS.TRIPS_STATUS, { tripId }, { headers })
      .subscribe({
        next: (res: any) => {
          this.zone.run(() => {
            const data = res?.data ?? res;
            // Handle both MATCHED (main app) and DRIVER_ARRIVING (location app) statuses
            if (data.status === 'MATCHED' || data.status === 'DRIVER_ARRIVING') {
              console.log('Driver found/arriving:', data);
              this.clearTimers();
              this.acceptedTrip = data;
              this.viewState = 'tracking';
              this.sourceLat = this.sourceLat || data.pickupLat;
              this.sourceLng = this.sourceLng || data.pickupLng;
              this.destLat = this.destLat || data.dropoffLat;
              this.destLng = this.destLng || data.dropoffLng;
              this.initTrackingMap();
              this.startDriverTracking(tripId);
              this.cdr.detectChanges();
            }
          });
        },
        error: (err: any) => {
          console.error('Poll error:', err);
        }
      });
  }

  // ── Driver Tracking: poll location service + completion status ────────
  startDriverTracking(tripId: string) {
    // Poll driver location from the location service
    this.trackingPollRef = setInterval(() => {
      const driverId = this.acceptedTrip?.driverId ?? this.acceptedTrip?.DriverId;
      const userId = this.auth.getUserId();
      const headers = new HttpHeaders(userId ? { 'X-User-Id': userId } : {});

      this.http.get<any>(API_URLS.LOCATION_DRIVER(driverId), { headers })
        .subscribe({
          next: (response) => {
            this.zone.run(() => {
              const data = response?.data ?? response;
              console.log('Driver location update:', data);

              if (data.latitude && data.longitude) {
                this.driverLat = data.latitude;
                this.driverLng = data.longitude;

                if (this.driverMarker) {
                  this.driverMarker.setLatLng([this.driverLat, this.driverLng]);
                  this.trackingMap?.panTo([this.driverLat, this.driverLng]);
                } else if (this.trackingMap) {
                  this.driverMarker = L.marker([this.driverLat, this.driverLng], {
                    icon: this.createCarIcon()
                  }).bindPopup('🚗 Driver').addTo(this.trackingMap);
                }

                this.checkDriverArrival();
              }

              this.cdr.detectChanges();
            });
          },
          error: (err) => {
            console.error('Error fetching driver location:', err);
          }
        });
    }, 6000);

    // Poll for trip completion — navigate to payment when done
    this.completionPollRef = setInterval(() => {
      const userId = this.auth.getUserId();
      const headers = new HttpHeaders({
        'Content-Type': 'application/json',
        ...(userId ? { 'X-User-Id': userId } : {})
      });

      this.http.post<any>(API_URLS.TRIPS_GET_COMPLETE, { tripId }, { headers })
        .subscribe({
          next: (res) => {
            this.zone.run(() => {
              console.log('Completion status check:', res);

              // Update trip status for UI
              if (res.status && this.acceptedTrip) {
                this.acceptedTrip.status = res.status;
              }

              if (res.status === 'COMPLETED') {
                console.log('Trip completed! Navigating to payment...');
                clearInterval(this.completionPollRef);
                this.completionPollRef = null;
                clearInterval(this.trackingPollRef);
                this.trackingPollRef = null;
                this.destroyTrackingMap();

                this.router.navigate(['/payment', tripId], {
                  state: {
                    riderId: userId,
                    amount: this.bookedTrip?.estimatedFare ?? this.fare
                  }
                });
              }

              // Also update driver location if available in completion response
              if (res.driverLat && res.driverLng && this.driverMarker) {
                this.driverLat = res.driverLat;
                this.driverLng = res.driverLng;
                this.driverMarker.setLatLng([this.driverLat, this.driverLng]);
                this.trackingMap?.panTo([this.driverLat, this.driverLng]);
              }

              this.cdr.detectChanges();
            });
          },
          error: (err) => {
            console.error('Error polling trip completion:', err);
          }
        });
    }, 3000);
  }

  private drawRouteToDestination() {
    if (!this.sourceLat || !this.destLat) return;

    const startLat = this.driverLat || this.sourceLat;
    const startLng = this.driverLng || this.sourceLng;

    const routeUrl = `https://router.project-osrm.org/route/v1/driving/${startLng},${startLat};${this.destLng},${this.destLat}?overview=full&geometries=geojson`;
    fetch(routeUrl)
      .then(res => res.json())
      .then((data) => {
        if (data.code === 'Ok' && data.routes?.length) {
          const coords = data.routes[0].geometry.coordinates.map((c: number[]) => [c[1], c[0]]);
          L.polyline(coords, { color: '#2563eb', weight: 5, opacity: 0.8 }).addTo(this.trackingMap);
        }
      })
      .catch((err) => console.warn('Route drawing failed:', err));
  }

  private checkDriverArrival() {
    const distance = this.calculateDistance(
      this.driverLat, this.driverLng,
      this.sourceLat, this.sourceLng
    );

    console.log('Distance to pickup:', distance.toFixed(2), 'km');

    if (distance < 0.5 && !this.driverArriving) {
      if (this.driverArrivingTimeoutRef) {
        clearTimeout(this.driverArrivingTimeoutRef);
      }
      this.driverArrivingTimeoutRef = setTimeout(() => {
        this.zone.run(() => {
          this.driverArriving = true;
          this.cdr.detectChanges();
          console.log('✅ Driver arriving!');
        });
      }, 2000);
    }
  }

  private calculateDistance(lat1: number, lng1: number, lat2: number, lng2: number): number {
    const R = 6371;
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLng = (lng2 - lng1) * Math.PI / 180;
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
      Math.sin(dLng / 2) * Math.sin(dLng / 2);
    return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }

  cancelSearch() {
    this.clearTimers();
    this.viewState = 'booking';
    this.noDriverFound = false;
    this.searchSecondsLeft = 120;

    const tripId = this.bookedTrip?.id ?? this.bookedTrip?.tripId;
    if (tripId) {
      const userId = this.auth.getUserId();
      const headers = new HttpHeaders({
        'Content-Type': 'application/json',
        ...(userId ? { 'X-User-Id': userId } : {})
      });
      this.http.put(API_URLS.TRIPS_BASE + `/${tripId}/cancel`, {}, { headers }).subscribe({ error: () => {} });
    }

    this.cdr.detectChanges();
  }

  // ── Icon helpers ──────────────────────────────────────────────────────
  createIcon(color: string) {
    const hex = color === 'green' ? '#16a34a' : '#dc2626';
    const svg = `
      <svg xmlns="http://www.w3.org/2000/svg" width="28" height="40" viewBox="0 0 28 40">
        <path d="M14 0C6.268 0 0 6.268 0 14c0 9.333 14 26 14 26S28 23.333 28 14
                 C28 6.268 21.732 0 14 0z" fill="${hex}" stroke="white" stroke-width="1.5"/>
        <circle cx="14" cy="14" r="5" fill="white"/>
      </svg>`;
    return L.divIcon({ html: svg, iconSize: [28, 40], iconAnchor: [14, 40], className: '' });
  }

  createCarIcon() {
    const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="36" height="36" viewBox="0 0 36 36">
      <circle cx="18" cy="18" r="17" fill="#2563eb" stroke="white" stroke-width="2"/>
      <text x="18" y="23" text-anchor="middle" font-size="16">🚗</text>
    </svg>`;
    return L.divIcon({ html: svg, iconSize: [36, 36], iconAnchor: [18, 18], className: '' });
  }
}
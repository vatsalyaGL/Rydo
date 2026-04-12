// ─────────────────────────────────────────────────────────────
//  API CONFIG — all base URLs in one place
//  Change BASE_* here to switch environments
// ─────────────────────────────────────────────────────────────

const USER_SERVICE    = 'http://localhost:8081';
const TRIP_SERVICE    = 'http://localhost:8082';
const LOCATION_SERVICE = 'http://localhost:8083';
const PAYMENT_SERVICE = 'http://localhost:8084';
const RATING_SERVICE  = 'http://localhost:8085';

export const API_URLS = {

  // ── Auth ───────────────────────────────────────────────────
  AUTH_BASE:          `${USER_SERVICE}/api/v1/auth`,
  DRIVER_ME:          `${USER_SERVICE}/api/v1/driver/me`,
  DRIVER_BASE:        `${USER_SERVICE}/api/v1/driver`,
  ADMIN_BASE:         `${USER_SERVICE}/api/admin`,
  USERS_BASE:         `${USER_SERVICE}/api/v1/users`,

  // ── Trips ──────────────────────────────────────────────────
  TRIPS_BASE:         `${TRIP_SERVICE}/api/trips`,
  TRIPS_STATUS:       `${TRIP_SERVICE}/api/trips/status`,
  TRIPS_NEARBY:       `${TRIP_SERVICE}/api/trips/nearby`,
  TRIPS_ACCEPT:       `${TRIP_SERVICE}/api/trips/accept-ride`,
  TRIPS_START:        `${TRIP_SERVICE}/api/trips/start-ride`,
  TRIPS_CANCEL_RIDE:  `${TRIP_SERVICE}/api/trips/cancel-ride`,
  TRIPS_COMPLETE:     `${TRIP_SERVICE}/api/trips/complete-status`,
  TRIPS_GET_COMPLETE: `${TRIP_SERVICE}/api/trips/get-complete`,

  // ── Location ───────────────────────────────────────────────
  LOCATION_UPDATE:    `${LOCATION_SERVICE}/api/v1/location/update`,
  LOCATION_DRIVER:    (driverId: string) => `${LOCATION_SERVICE}/api/v1/location/driver/${driverId}`,

  // ── Payments ───────────────────────────────────────────────
  PAYMENTS_BASE:      `${PAYMENT_SERVICE}/api/payments`,
  PAYMENT_BY_TRIP:    (tripId: string) => `${PAYMENT_SERVICE}/api/payments/${tripId}`,

  // ── Ratings ────────────────────────────────────────────────
  RATINGS_BASE:       `${RATING_SERVICE}/api/v1/ratings`,
  RATINGS_BY_USER:    (userId: string) => `${RATING_SERVICE}/api/v1/ratings/user/${userId}`,

} as const;

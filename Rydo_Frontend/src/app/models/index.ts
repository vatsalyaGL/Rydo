export interface AuthResponseDTO {
  token: string;
  userId: string;
  role: string;
}

export interface RequestOtpDTO {
  phoneNumber: string;
}

export interface VerifyOtpDTO {
  phoneNumber: string;
  otp: string;
}

export interface UserResponseDTO {
  id: string;
  fullName: string;
  phoneNumber: string;
  email: string;
  role: string;
  ratingAvg: number;
}

export interface UserCreateDTO {
  phoneNumber: string;
  fullName: string;
}

export interface UserUpdateDTO {
  fullName?: string;
  email?: string;
}

export interface DriverApplyDTO {
  licenseNumber: string;
  vehicleMake: string;
  vehicleModel: string;
  vehicleYear: number;
  vehicleColor: string;
  vehiclePlate: string;
  vehicleType: string;
  cityId: number;
}

export interface DriverResponseDTO {
  userId: string;
  fullName: string;
  email?: string;
  phoneNumber?: string;
  vehicleMake: string;
  vehicleModel: string;
  vehicleYear?: number;
  vehicleColor?: string;
  vehiclePlate: string;
  vehicleType: string;
  cityId?: number;
  verificationStatus: string;
}

export type VehicleType = 'ECONOMY' | 'COMFORT' | 'XL' | 'BLACK' | 'MOTO' | 'AUTO';
export type VerificationStatus = 'PENDING' | 'DOCS_SUBMITTED' | 'UNDER_REVIEW' | 'BACKGROUND_CHECK' | 'VERIFIED' | 'REJECTED';
export type Role = 'USER' | 'DRIVER' | 'ADMIN';

export interface NearbyRiderDTO {
  tripId: string;
  riderId: string;
  riderName: string;
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

export interface TripResponseDTO {
  id: string;
  riderId: string;
  driverId?: string;
  status: string;
  pickupLat: number;
  pickupLng: number;
  pickupAddress: string;
  dropoffLat: number;
  dropoffLng: number;
  dropoffAddress: string;
  estimatedDistanceKm: number;
  estimatedFare: number;
  vehicleType: string;
  driverLat?: number;
  driverLng?: number;
}

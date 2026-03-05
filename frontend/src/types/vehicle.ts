export type VehicleType = 'CAR' | 'SUV' | 'VAN' | 'BUS' | 'TUK_TUK' | 'MOTORBIKE'

export interface Vehicle {
  id: number
  ownerId: string
  make: string
  model: string
  year: number
  vehicleType: VehicleType
  licensePlate: string
  seatingCapacity: number
  dailyRate: number
  features?: string[]
  images?: string[]
  isAvailable: boolean
  isActive: boolean
  averageRating: number
  reviewCount: number
  createdAt: string
  updatedAt?: string
}

export interface CreateVehicleRequest {
  make: string
  model: string
  year: number
  vehicleType: VehicleType
  licensePlate: string
  seatingCapacity: number
  dailyRate: number
  features?: string[]
  isAvailable?: boolean
}

export interface VehicleUpdateRequest extends Partial<CreateVehicleRequest> {
  isAvailable?: boolean
}

export interface VehicleSearchParams {
  vehicleType?: string
  minCapacity?: number
  minDailyRate?: number
  maxDailyRate?: number
  query?: string
  page?: number
  size?: number
}

export const VEHICLE_TYPES: VehicleType[] = ['CAR', 'SUV', 'VAN', 'BUS', 'TUK_TUK', 'MOTORBIKE']

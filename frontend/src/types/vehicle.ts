export type VehicleType = 'CAR' | 'VAN' | 'BUS' | 'MOTORCYCLE' | 'TUKTU' | 'JEEP' | 'SUV'

export interface Vehicle {
  id: number
  ownerId: string
  make: string
  model: string
  year: number
  vehicleType: VehicleType
  licensePlate: string
  capacity: number
  pricePerDay: number
  description?: string
  features?: string[]
  imageUrl?: string
  isAvailable: boolean
  isActive: boolean
  averageRating: number
  reviewCount: number
  city?: string
  createdAt: string
}

export interface CreateVehicleRequest {
  make: string
  model: string
  year: number
  vehicleType: VehicleType
  licensePlate: string
  capacity: number
  pricePerDay: number
  description?: string
  features?: string[]
  city?: string
}

export interface VehicleUpdateRequest extends Partial<CreateVehicleRequest> {
  isAvailable?: boolean
}

export interface VehicleSearchParams {
  vehicleType?: string
  minCapacity?: number
  maxPricePerDay?: number
  city?: string
  page?: number
  size?: number
}

export const VEHICLE_TYPES: VehicleType[] = ['CAR', 'VAN', 'BUS', 'MOTORCYCLE', 'TUKTU', 'JEEP', 'SUV']

export interface Hotel {
  id: number
  ownerId: string
  name: string
  description: string | null
  address: string
  city: string
  latitude: number | null
  longitude: number | null
  starRating: number | null
  averageRating: number
  reviewCount: number
  amenities: string[]
  imageUrl: string | null
  checkInTime: string
  checkOutTime: string
  isActive: boolean
  rooms: Room[]
  createdAt: string
  updatedAt: string
}

export interface Room {
  id: number
  hotelId: number
  roomType: string
  name: string
  description: string | null
  pricePerNight: number
  maxOccupancy: number
  amenities: string[]
  totalRooms: number
  isActive: boolean
  createdAt: string
  updatedAt: string
}

export interface HotelAvailability {
  hotelId: number
  hotelName: string
  available: boolean
  availableRooms: number
  message: string
}

export interface CreateHotelRequest {
  name: string
  description?: string
  address: string
  city: string
  latitude?: number
  longitude?: number
  starRating?: number
  amenities?: string[]
  imageUrl?: string
  checkInTime?: string
  checkOutTime?: string
}

export interface UpdateHotelRequest {
  name?: string
  description?: string
  address?: string
  city?: string
  latitude?: number
  longitude?: number
  starRating?: number
  amenities?: string[]
  imageUrl?: string
  checkInTime?: string
  checkOutTime?: string
  isActive?: boolean
}

export interface CreateRoomRequest {
  hotelId: number
  roomType: string
  name: string
  description?: string
  pricePerNight: number
  maxOccupancy: number
  amenities?: string[]
  totalRooms: number
}

export interface UpdateRoomRequest {
  roomType?: string
  name?: string
  description?: string
  pricePerNight?: number
  maxOccupancy?: number
  amenities?: string[]
  totalRooms?: number
  isActive?: boolean
}

export type RoomType = 'STANDARD' | 'DELUXE' | 'SUITE' | 'FAMILY' | 'DORMITORY'

export const ROOM_TYPES: { value: RoomType; label: string }[] = [
  { value: 'STANDARD', label: 'Standard' },
  { value: 'DELUXE', label: 'Deluxe' },
  { value: 'SUITE', label: 'Suite' },
  { value: 'FAMILY', label: 'Family' },
  { value: 'DORMITORY', label: 'Dormitory' },
]

export const HOTEL_AMENITIES = [
  'wifi', 'pool', 'spa', 'restaurant', 'parking', 'gym',
  'air_conditioning', 'minibar', 'balcony', 'jacuzzi', 'butler_service',
] as const

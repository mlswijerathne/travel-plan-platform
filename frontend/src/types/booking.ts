export type BookingStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED'
export type ItemStatus = 'PENDING' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED'
export type ProviderType = 'HOTEL' | 'TOUR_GUIDE' | 'VEHICLE'
export type RefundPolicy = 'FULL_REFUND' | 'PARTIAL_REFUND' | 'NO_REFUND'

export interface Booking {
  id: number
  touristId: string
  bookingReference: string
  itineraryId: number | null
  status: BookingStatus
  totalAmount: number
  bookingDate: string
  startDate: string
  endDate: string
  notes: string | null
  cancellationReason: string | null
  refundAmount: number | null
  refundPolicy: RefundPolicy | null
  items: BookingItem[]
  createdAt: string
  updatedAt: string
}

export interface BookingItem {
  id: number
  providerType: ProviderType
  providerId: number
  itemName: string
  quantity: number
  unitPrice: number
  subtotal: number
  startDate: string | null
  endDate: string | null
  status: ItemStatus
  createdAt: string
}

export interface CreateBookingRequest {
  startDate: string
  endDate: string
  itineraryId?: number
  notes?: string
  items: BookingItemRequest[]
}

export interface BookingItemRequest {
  providerType: ProviderType
  providerId: number
  itemName: string
  quantity?: number
  unitPrice: number
  startDate?: string
  endDate?: string
}

export interface CancelBookingRequest {
  reason?: string
}

export const BOOKING_STATUSES: { value: BookingStatus | 'ALL'; label: string }[] = [
  { value: 'ALL', label: 'All' },
  { value: 'PENDING', label: 'Pending' },
  { value: 'CONFIRMED', label: 'Confirmed' },
  { value: 'COMPLETED', label: 'Completed' },
  { value: 'CANCELLED', label: 'Cancelled' },
]

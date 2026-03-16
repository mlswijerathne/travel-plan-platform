import type { ApiResponse, PaginatedResponse } from '@/types/api'
import type { Booking, CreateBookingRequest, CancelBookingRequest, BookingStatus } from '@/types/booking'
import { apiGet, apiPost, apiPut, buildQueryString } from './client'

export function createBooking(data: CreateBookingRequest) {
  return apiPost<ApiResponse<Booking>>('/api/bookings', data)
}

export function getBooking(id: number) {
  return apiGet<ApiResponse<Booking>>(`/api/bookings/${id}`)
}

export function getMyBookings(params: { status?: BookingStatus; page?: number; size?: number } = {}) {
  const qs = buildQueryString(params)
  return apiGet<PaginatedResponse<Booking>>(`/api/bookings${qs}`)
}

export function cancelBooking(id: number, data: CancelBookingRequest = {}) {
  return apiPost<ApiResponse<Booking>>(`/api/bookings/${id}/cancel`, data)
}

export function getProviderBookings(
  providerType: string,
  providerId: number,
  params: { status?: string; page?: number; size?: number } = {}
) {
  const qs = buildQueryString(params)
  return apiGet<PaginatedResponse<Booking>>(`/api/bookings/provider/${providerType}/${providerId}${qs}`)
}

export function getBookingByReference(reference: string) {
  return apiGet<ApiResponse<Booking>>(`/api/bookings/reference/${reference}`)
}

export function getBookingDetails(id: number) {
  return apiGet<ApiResponse<Booking>>(`/api/bookings/${id}/details`)
}

export function updateBookingItemStatus(
  bookingId: number,
  itemId: number,
  status: string,
  reason?: string
) {
  return apiPut<ApiResponse<Booking>>(`/api/bookings/${bookingId}/items/${itemId}/status`, { status, reason })
}

export function completeBooking(id: number) {
  return apiPost<ApiResponse<Booking>>(`/api/bookings/${id}/complete`, {})
}

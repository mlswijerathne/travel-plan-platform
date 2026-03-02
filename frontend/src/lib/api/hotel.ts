import type { ApiResponse, PaginatedResponse } from '@/types/api'
import type { Hotel, Room, HotelAvailability, CreateHotelRequest, UpdateHotelRequest } from '@/types/hotel'
import { apiGet, apiPost, apiPut, apiDelete, buildQueryString } from './client'

export interface HotelSearchParams {
  city?: string
  minStarRating?: number
  maxPrice?: number
  minPrice?: number
  amenities?: string
  checkIn?: string
  checkOut?: string
  guests?: number
  page?: number
  size?: number
  sort?: string
}

export interface HotelQueryParams {
  q?: string
  page?: number
  size?: number
}

export function searchHotels(params: HotelSearchParams = {}) {
  const qs = buildQueryString(params)
  return apiGet<PaginatedResponse<Hotel>>(`/api/hotels${qs}`)
}

export function queryHotels(params: HotelQueryParams = {}) {
  const qs = buildQueryString(params)
  return apiGet<PaginatedResponse<Hotel>>(`/api/hotels/query${qs}`)
}

export function getHotel(id: number) {
  return apiGet<ApiResponse<Hotel>>(`/api/hotels/${id}`)
}

export function getHotelDetails(id: number) {
  return apiGet<ApiResponse<Hotel>>(`/api/hotels/${id}/details`)
}

export function getMyHotels(params: { page?: number; size?: number } = {}) {
  const qs = buildQueryString(params)
  return apiGet<PaginatedResponse<Hotel>>(`/api/hotels/owner${qs}`)
}

export function createHotel(data: CreateHotelRequest) {
  return apiPost<ApiResponse<Hotel>>('/api/hotels', data)
}

export function updateHotel(id: number, data: UpdateHotelRequest) {
  return apiPut<ApiResponse<Hotel>>(`/api/hotels/${id}`, data)
}

export function deleteHotel(id: number) {
  return apiDelete(`/api/hotels/${id}`)
}

export function checkHotelAvailability(id: number, startDate: string, endDate: string) {
  const qs = buildQueryString({ startDate, endDate })
  return apiGet<ApiResponse<HotelAvailability>>(`/api/hotels/${id}/availability${qs}`)
}

export function getRoomsByHotel(hotelId: number) {
  return apiGet<ApiResponse<Room[]>>(`/api/rooms/hotel/${hotelId}`)
}

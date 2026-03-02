import type { ApiResponse } from '@/types/api'
import type { Room, CreateRoomRequest, UpdateRoomRequest } from '@/types/hotel'
import { apiGet, apiPost, apiPut, apiDelete } from './client'

export function createRoom(data: CreateRoomRequest) {
  return apiPost<ApiResponse<Room>>('/api/rooms', data)
}

export function getRoom(id: number) {
  return apiGet<ApiResponse<Room>>(`/api/rooms/${id}`)
}

export function getRoomsByHotel(hotelId: number) {
  return apiGet<ApiResponse<Room[]>>(`/api/rooms/hotel/${hotelId}`)
}

export function updateRoom(id: number, data: UpdateRoomRequest) {
  return apiPut<ApiResponse<Room>>(`/api/rooms/${id}`, data)
}

export function deleteRoom(id: number) {
  return apiDelete(`/api/rooms/${id}`)
}

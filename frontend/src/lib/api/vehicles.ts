import { apiGet, apiPost, apiPut, apiDelete, buildQueryString } from './client'
import type { Vehicle, CreateVehicleRequest, VehicleUpdateRequest, VehicleSearchParams } from '@/types/vehicle'
import type { PaginatedResponse } from '@/types/api'

export function searchVehicles(params?: VehicleSearchParams): Promise<PaginatedResponse<Vehicle>> {
  return apiGet(`/api/vehicles${buildQueryString(params ?? {})}`)
}

export function getVehicle(id: number): Promise<Vehicle> {
  return apiGet(`/api/vehicles/${id}`)
}

export function getMyVehicles(): Promise<PaginatedResponse<Vehicle>> {
  return apiGet('/api/vehicles/owner')
}

export function createVehicle(body: CreateVehicleRequest): Promise<Vehicle> {
  return apiPost('/api/vehicles', body)
}

export function updateVehicle(id: number, body: VehicleUpdateRequest): Promise<Vehicle> {
  return apiPut(`/api/vehicles/${id}`, body)
}

export function deleteVehicle(id: number): Promise<void> {
  return apiDelete(`/api/vehicles/${id}`)
}

export function checkVehicleAvailability(id: number, params: {
  startDate: string
  endDate: string
}): Promise<{ available: boolean }> {
  return apiGet(`/api/vehicles/${id}/availability${buildQueryString(params)}`)
}

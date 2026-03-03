import { apiGet, apiPost, apiPut, apiDelete, buildQueryString } from './client'
import type { TripPackage } from '@/types/trip-package'
import type { PaginatedResponse } from '@/types/api'

export interface PackageRequest {
  name: string
  description: string
  durationDays: number
  basePrice: number
  discountPercentage?: number
  maxParticipants: number
  destinations: string[]
  inclusions: string[]
  exclusions: string[]
  images?: string[]
  isFeatured?: boolean
}

export function getPackages(params?: {
  page?: number
  size?: number
}): Promise<PaginatedResponse<TripPackage>> {
  return apiGet(`/api/packages${buildQueryString(params ?? {})}`)
}

export function getFeaturedPackages(): Promise<{ data: TripPackage[] }> {
  return apiGet('/api/packages/featured')
}

export function getPackage(id: number): Promise<{ data: TripPackage }> {
  return apiGet(`/api/packages/${id}`)
}

export function createPackage(body: PackageRequest): Promise<{ data: TripPackage }> {
  return apiPost('/api/packages', body)
}

export function updatePackage(id: number, body: PackageRequest): Promise<{ data: TripPackage }> {
  return apiPut(`/api/packages/${id}`, body)
}

export function deletePackage(id: number): Promise<void> {
  return apiDelete(`/api/packages/${id}`)
}

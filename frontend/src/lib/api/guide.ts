import type { ApiResponse, PaginatedResponse } from '@/types/api'
import type { Guide, GuideAvailability, GuideCreateRequest, GuideUpdateRequest } from '@/types/guide'
import { apiGet, apiPost, apiPut, apiDelete, buildQueryString } from './client'

export interface GuideSearchParams {
  language?: string
  specialization?: string
  minRating?: number
  maxHourlyRate?: number
  maxDailyRate?: number
  isVerified?: boolean
  query?: string
  page?: number
  size?: number
}

export function searchGuides(params: GuideSearchParams = {}) {
  const qs = buildQueryString(params)
  return apiGet<PaginatedResponse<Guide>>(`/api/tour-guides${qs}`)
}

export function getGuide(id: number) {
  return apiGet<ApiResponse<Guide>>(`/api/tour-guides/${id}`)
}

export function getMyGuideProfile() {
  return apiGet<ApiResponse<Guide>>('/api/tour-guides/me')
}

export function registerGuide(data: GuideCreateRequest) {
  return apiPost<ApiResponse<Guide>>('/api/tour-guides', data)
}

export function updateMyGuideProfile(data: GuideUpdateRequest) {
  return apiPut<ApiResponse<Guide>>('/api/tour-guides/me', data)
}

export function deleteGuide(id: number) {
  return apiDelete(`/api/tour-guides/${id}`)
}

export function checkGuideAvailability(id: number, startDate: string, endDate: string) {
  const qs = buildQueryString({ startDate, endDate })
  return apiGet<ApiResponse<GuideAvailability>>(`/api/tour-guides/${id}/availability${qs}`)
}

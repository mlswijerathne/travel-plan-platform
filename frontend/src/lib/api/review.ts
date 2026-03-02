import type { ApiResponse, PaginatedResponse } from '@/types/api'
import type { Review, ReviewSummary, PendingReview, CreateReviewRequest, UpdateReviewRequest, ProviderResponseRequest, ProviderResponse } from '@/types/review'
import { apiGet, apiPost, apiPut, apiDelete, buildQueryString } from './client'

export function createReview(data: CreateReviewRequest) {
  return apiPost<ApiResponse<Review>>('/api/reviews', data)
}

export function updateReview(id: number, data: UpdateReviewRequest) {
  return apiPut<ApiResponse<Review>>(`/api/reviews/${id}`, data)
}

export function deleteReview(id: number) {
  return apiDelete(`/api/reviews/${id}`)
}

export function getReview(id: number) {
  return apiGet<ApiResponse<Review>>(`/api/reviews/${id}`)
}

export function getMyReviews(params: { page?: number; size?: number } = {}) {
  const qs = buildQueryString(params)
  return apiGet<ApiResponse<PaginatedResponse<Review>>>(`/api/reviews/my${qs}`)
}

export function getPendingReviews(params: { page?: number; size?: number } = {}) {
  const qs = buildQueryString(params)
  return apiGet<ApiResponse<PaginatedResponse<PendingReview>>>(`/api/reviews/pending${qs}`)
}

export function getReviewsByEntity(entityType: string, entityId: number, params: { page?: number; size?: number } = {}) {
  const qs = buildQueryString(params)
  return apiGet<ApiResponse<PaginatedResponse<Review>>>(`/api/reviews/entity/${entityType}/${entityId}${qs}`)
}

export function getReviewSummary(entityType: string, entityId: number) {
  return apiGet<ApiResponse<ReviewSummary>>(`/api/reviews/summary/${entityType}/${entityId}`)
}

export function addProviderResponse(reviewId: number, data: ProviderResponseRequest) {
  return apiPost<ApiResponse<ProviderResponse>>(`/api/reviews/${reviewId}/responses`, data)
}

export function deleteProviderResponse(reviewId: number, responseId: number) {
  return apiDelete(`/api/reviews/${reviewId}/responses/${responseId}`)
}

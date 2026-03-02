'use client'

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getReviewsByEntity, getReviewSummary, getMyReviews, getPendingReviews, createReview, updateReview, deleteReview, addProviderResponse, deleteProviderResponse } from '@/lib/api/review'
import type { CreateReviewRequest, UpdateReviewRequest, ProviderResponseRequest } from '@/types/review'

export function useReviewsByEntity(entityType: string, entityId: number, params: { page?: number; size?: number } = {}) {
  return useQuery({
    queryKey: ['reviews', entityType, entityId, params],
    queryFn: () => getReviewsByEntity(entityType, entityId, params),
    enabled: !!entityType && !!entityId,
  })
}

export function useReviewSummary(entityType: string, entityId: number) {
  return useQuery({
    queryKey: ['reviewSummary', entityType, entityId],
    queryFn: () => getReviewSummary(entityType, entityId),
    enabled: !!entityType && !!entityId,
  })
}

export function useMyReviews(params: { page?: number; size?: number } = {}) {
  return useQuery({
    queryKey: ['myReviews', params],
    queryFn: () => getMyReviews(params),
  })
}

export function usePendingReviews(params: { page?: number; size?: number } = {}) {
  return useQuery({
    queryKey: ['pendingReviews', params],
    queryFn: () => getPendingReviews(params),
  })
}

export function useCreateReview() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (data: CreateReviewRequest) => createReview(data),
    onSuccess: (_, data) => {
      queryClient.invalidateQueries({ queryKey: ['reviews', data.entityType, data.entityId] })
      queryClient.invalidateQueries({ queryKey: ['reviewSummary', data.entityType, data.entityId] })
      queryClient.invalidateQueries({ queryKey: ['myReviews'] })
      queryClient.invalidateQueries({ queryKey: ['pendingReviews'] })
    },
  })
}

export function useUpdateReview() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateReviewRequest }) => updateReview(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['reviews'] })
      queryClient.invalidateQueries({ queryKey: ['reviewSummary'] })
      queryClient.invalidateQueries({ queryKey: ['myReviews'] })
    },
  })
}

export function useDeleteReview() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => deleteReview(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['reviews'] })
      queryClient.invalidateQueries({ queryKey: ['reviewSummary'] })
      queryClient.invalidateQueries({ queryKey: ['myReviews'] })
    },
  })
}

export function useAddProviderResponse() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ reviewId, data }: { reviewId: number; data: ProviderResponseRequest }) => addProviderResponse(reviewId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['reviews'] })
    },
  })
}

export function useDeleteProviderResponse() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ reviewId, responseId }: { reviewId: number; responseId: number }) => deleteProviderResponse(reviewId, responseId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['reviews'] })
    },
  })
}

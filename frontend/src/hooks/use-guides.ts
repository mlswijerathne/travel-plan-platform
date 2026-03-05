'use client'

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { searchGuides, getGuide, getMyGuideProfile, registerGuide, updateMyGuideProfile, checkGuideAvailability, type GuideSearchParams } from '@/lib/api/guide'
import type { GuideCreateRequest, GuideUpdateRequest } from '@/types/guide'

export function useGuides(params: GuideSearchParams = {}) {
  return useQuery({
    queryKey: ['guides', params],
    queryFn: () => searchGuides(params),
  })
}

export function useGuide(id: number) {
  return useQuery({
    queryKey: ['guide', id],
    queryFn: () => getGuide(id),
    enabled: !!id,
  })
}

export function useMyGuideProfile(options?: { enabled?: boolean }) {
  return useQuery({
    queryKey: ['myGuideProfile'],
    queryFn: () => getMyGuideProfile(),
    retry: false,
    enabled: options?.enabled !== false,
  })
}

export function useGuideAvailability(id: number, startDate: string, endDate: string) {
  return useQuery({
    queryKey: ['guideAvailability', id, startDate, endDate],
    queryFn: () => checkGuideAvailability(id, startDate, endDate),
    enabled: !!id && !!startDate && !!endDate,
  })
}

export function useRegisterGuide() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (data: GuideCreateRequest) => registerGuide(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['myGuideProfile'] })
    },
  })
}

export function useUpdateMyGuideProfile() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (data: GuideUpdateRequest) => updateMyGuideProfile(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['myGuideProfile'] })
      queryClient.invalidateQueries({ queryKey: ['guides'] })
    },
  })
}

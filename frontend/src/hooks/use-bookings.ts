'use client'

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getMyBookings, getBooking, createBooking, cancelBooking, getProviderBookings } from '@/lib/api/booking'
import type { CreateBookingRequest, CancelBookingRequest, BookingStatus } from '@/types/booking'

export function useMyBookings(params: { status?: BookingStatus; page?: number; size?: number } = {}) {
  return useQuery({
    queryKey: ['myBookings', params],
    queryFn: () => getMyBookings(params),
  })
}

export function useBooking(id: number) {
  return useQuery({
    queryKey: ['booking', id],
    queryFn: () => getBooking(id),
    enabled: !!id,
  })
}

export function useProviderBookings(
  providerType: string,
  providerId: number,
  params: { status?: string; page?: number; size?: number } = {}
) {
  return useQuery({
    queryKey: ['providerBookings', providerType, providerId, params],
    queryFn: () => getProviderBookings(providerType, providerId, params),
    enabled: !!providerType && !!providerId,
  })
}

export function useCreateBooking() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (data: CreateBookingRequest) => createBooking(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['myBookings'] })
    },
  })
}

export function useCancelBooking() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data?: CancelBookingRequest }) => cancelBooking(id, data),
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: ['booking', id] })
      queryClient.invalidateQueries({ queryKey: ['myBookings'] })
    },
  })
}

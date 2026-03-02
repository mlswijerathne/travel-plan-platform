'use client'

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { searchHotels, getHotel, getHotelDetails, getMyHotels, createHotel, updateHotel, deleteHotel, checkHotelAvailability, type HotelSearchParams } from '@/lib/api/hotel'
import type { CreateHotelRequest, UpdateHotelRequest } from '@/types/hotel'

export function useHotels(params: HotelSearchParams = {}) {
  return useQuery({
    queryKey: ['hotels', params],
    queryFn: () => searchHotels(params),
  })
}

export function useHotel(id: number) {
  return useQuery({
    queryKey: ['hotel', id],
    queryFn: () => getHotel(id),
    enabled: !!id,
  })
}

export function useHotelDetails(id: number) {
  return useQuery({
    queryKey: ['hotel', id, 'details'],
    queryFn: () => getHotelDetails(id),
    enabled: !!id,
  })
}

export function useMyHotels(params: { page?: number; size?: number } = {}) {
  return useQuery({
    queryKey: ['myHotels', params],
    queryFn: () => getMyHotels(params),
  })
}

export function useHotelAvailability(id: number, startDate: string, endDate: string) {
  return useQuery({
    queryKey: ['hotelAvailability', id, startDate, endDate],
    queryFn: () => checkHotelAvailability(id, startDate, endDate),
    enabled: !!id && !!startDate && !!endDate,
  })
}

export function useCreateHotel() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (data: CreateHotelRequest) => createHotel(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['myHotels'] })
      queryClient.invalidateQueries({ queryKey: ['hotels'] })
    },
  })
}

export function useUpdateHotel() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateHotelRequest }) => updateHotel(id, data),
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: ['hotel', id] })
      queryClient.invalidateQueries({ queryKey: ['myHotels'] })
      queryClient.invalidateQueries({ queryKey: ['hotels'] })
    },
  })
}

export function useDeleteHotel() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => deleteHotel(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['myHotels'] })
      queryClient.invalidateQueries({ queryKey: ['hotels'] })
    },
  })
}

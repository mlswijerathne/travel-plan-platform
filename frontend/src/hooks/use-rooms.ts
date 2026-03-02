'use client'

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getRoomsByHotel, createRoom, updateRoom, deleteRoom } from '@/lib/api/room'
import type { CreateRoomRequest, UpdateRoomRequest } from '@/types/hotel'

export function useRoomsByHotel(hotelId: number) {
  return useQuery({
    queryKey: ['rooms', hotelId],
    queryFn: () => getRoomsByHotel(hotelId),
    enabled: !!hotelId,
  })
}

export function useCreateRoom() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (data: CreateRoomRequest) => createRoom(data),
    onSuccess: (_, data) => {
      queryClient.invalidateQueries({ queryKey: ['rooms', data.hotelId] })
      queryClient.invalidateQueries({ queryKey: ['hotel', data.hotelId] })
    },
  })
}

export function useUpdateRoom(hotelId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateRoomRequest }) => updateRoom(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['rooms', hotelId] })
      queryClient.invalidateQueries({ queryKey: ['hotel', hotelId] })
    },
  })
}

export function useDeleteRoom(hotelId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => deleteRoom(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['rooms', hotelId] })
      queryClient.invalidateQueries({ queryKey: ['hotel', hotelId] })
    },
  })
}

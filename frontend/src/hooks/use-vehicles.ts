'use client'

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  searchVehicles,
  getVehicle,
  getMyVehicles,
  createVehicle,
  updateVehicle,
  deleteVehicle,
} from '@/lib/api/vehicles'
import type { CreateVehicleRequest, VehicleUpdateRequest, VehicleSearchParams, Vehicle } from '@/types/vehicle'

export function useVehicles(params: VehicleSearchParams = {}) {
  return useQuery({
    queryKey: ['vehicles', params],
    queryFn: () => searchVehicles(params),
  })
}

export function useVehicle(id: number) {
  return useQuery({
    queryKey: ['vehicle', id],
    queryFn: () => getVehicle(id),
    enabled: !!id,
  })
}

export function useMyVehicles() {
  return useQuery<Vehicle[]>({
    queryKey: ['myVehicles'],
    queryFn: () => getMyVehicles(),
  })
}

export function useCreateVehicle() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (data: CreateVehicleRequest) => createVehicle(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['myVehicles'] })
      queryClient.invalidateQueries({ queryKey: ['vehicles'] })
    },
  })
}

export function useUpdateVehicle() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: VehicleUpdateRequest }) => updateVehicle(id, data),
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: ['vehicle', id] })
      queryClient.invalidateQueries({ queryKey: ['myVehicles'] })
      queryClient.invalidateQueries({ queryKey: ['vehicles'] })
    },
  })
}

export function useDeleteVehicle() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => deleteVehicle(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['myVehicles'] })
      queryClient.invalidateQueries({ queryKey: ['vehicles'] })
    },
  })
}

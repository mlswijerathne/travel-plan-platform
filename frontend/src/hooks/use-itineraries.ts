'use client'

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  getItineraries,
  getItinerary,
  createItinerary,
  updateItinerary,
  deleteItinerary,
  activateItinerary,
  completeItinerary,
  getItineraryDays,
  generateDays,
  getDayActivities,
  addActivity,
  deleteActivity,
  getExpenses,
  getExpenseSummary,
  addExpense,
  deleteExpense,
} from '@/lib/api/itinerary'
import type {
  CreateItineraryRequest,
  ItineraryDTO,
  ItineraryActivity,
  CreateExpenseRequest,
} from '@/types/itinerary'

export function useItineraries() {
  return useQuery({
    queryKey: ['itineraries'],
    queryFn: getItineraries,
  })
}

export function useItinerary(id: number) {
  return useQuery({
    queryKey: ['itinerary', id],
    queryFn: () => getItinerary(id),
    enabled: !!id,
  })
}

export function useCreateItinerary() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (data: CreateItineraryRequest) => createItinerary(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['itineraries'] })
    },
  })
}

export function useUpdateItinerary() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<ItineraryDTO> }) => updateItinerary(id, data),
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: ['itinerary', id] })
      queryClient.invalidateQueries({ queryKey: ['itineraries'] })
    },
  })
}

export function useDeleteItinerary() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => deleteItinerary(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['itineraries'] })
    },
  })
}

export function useActivateItinerary() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => activateItinerary(id),
    onSuccess: (_, id) => {
      queryClient.invalidateQueries({ queryKey: ['itinerary', id] })
      queryClient.invalidateQueries({ queryKey: ['itineraries'] })
    },
  })
}

export function useCompleteItinerary() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => completeItinerary(id),
    onSuccess: (_, id) => {
      queryClient.invalidateQueries({ queryKey: ['itinerary', id] })
      queryClient.invalidateQueries({ queryKey: ['itineraries'] })
    },
  })
}

export function useItineraryDays(itineraryId: number) {
  return useQuery({
    queryKey: ['itineraryDays', itineraryId],
    queryFn: () => getItineraryDays(itineraryId),
    enabled: !!itineraryId,
  })
}

export function useGenerateDays() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (itineraryId: number) => generateDays(itineraryId),
    onSuccess: (_, itineraryId) => {
      queryClient.invalidateQueries({ queryKey: ['itineraryDays', itineraryId] })
      queryClient.invalidateQueries({ queryKey: ['itinerary', itineraryId] })
    },
  })
}

export function useDayActivities(itineraryId: number, dayId: number) {
  return useQuery({
    queryKey: ['dayActivities', itineraryId, dayId],
    queryFn: () => getDayActivities(itineraryId, dayId),
    enabled: !!itineraryId && !!dayId,
  })
}

export function useAddActivity() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ itineraryId, dayId, data }: { itineraryId: number; dayId: number; data: Partial<ItineraryActivity> }) =>
      addActivity(itineraryId, dayId, data),
    onSuccess: (_, { itineraryId, dayId }) => {
      queryClient.invalidateQueries({ queryKey: ['dayActivities', itineraryId, dayId] })
      queryClient.invalidateQueries({ queryKey: ['itineraryDays', itineraryId] })
      queryClient.invalidateQueries({ queryKey: ['itinerary', itineraryId] })
    },
  })
}

export function useDeleteActivity() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ itineraryId, activityId }: { itineraryId: number; activityId: number }) =>
      deleteActivity(itineraryId, activityId),
    onSuccess: (_, { itineraryId }) => {
      queryClient.invalidateQueries({ queryKey: ['dayActivities'] })
      queryClient.invalidateQueries({ queryKey: ['itineraryDays', itineraryId] })
      queryClient.invalidateQueries({ queryKey: ['itinerary', itineraryId] })
    },
  })
}

export function useExpenses(itineraryId: number) {
  return useQuery({
    queryKey: ['expenses', itineraryId],
    queryFn: () => getExpenses(itineraryId),
    enabled: !!itineraryId,
  })
}

export function useExpenseSummary(itineraryId: number) {
  return useQuery({
    queryKey: ['expenseSummary', itineraryId],
    queryFn: () => getExpenseSummary(itineraryId),
    enabled: !!itineraryId,
  })
}

export function useAddExpense() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ itineraryId, data }: { itineraryId: number; data: CreateExpenseRequest }) =>
      addExpense(itineraryId, data),
    onSuccess: (_, { itineraryId }) => {
      queryClient.invalidateQueries({ queryKey: ['expenses', itineraryId] })
      queryClient.invalidateQueries({ queryKey: ['expenseSummary', itineraryId] })
      queryClient.invalidateQueries({ queryKey: ['itinerary', itineraryId] })
    },
  })
}

export function useDeleteExpense() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ itineraryId, expenseId }: { itineraryId: number; expenseId: number }) =>
      deleteExpense(itineraryId, expenseId),
    onSuccess: (_, { itineraryId }) => {
      queryClient.invalidateQueries({ queryKey: ['expenses', itineraryId] })
      queryClient.invalidateQueries({ queryKey: ['expenseSummary', itineraryId] })
      queryClient.invalidateQueries({ queryKey: ['itinerary', itineraryId] })
    },
  })
}

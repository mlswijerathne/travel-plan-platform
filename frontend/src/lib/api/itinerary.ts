import { apiGet, apiPost, apiPut, apiDelete } from './client'
import type {
  ItineraryDTO,
  ItineraryDay,
  ItineraryActivity,
  ItineraryExpense,
  ExpenseSummary,
  CreateItineraryRequest,
  CreateExpenseRequest,
} from '@/types/itinerary'

const BASE = '/api/v1/itineraries'

// Itineraries
export function getItineraries(): Promise<ItineraryDTO[]> {
  return apiGet(BASE)
}

export function getActiveItineraries(): Promise<ItineraryDTO[]> {
  return apiGet(`${BASE}/active`)
}

export function getItinerary(id: number): Promise<ItineraryDTO> {
  return apiGet(`${BASE}/${id}`)
}

export function createItinerary(body: CreateItineraryRequest): Promise<ItineraryDTO> {
  return apiPost(BASE, body)
}

export function updateItinerary(id: number, body: Partial<ItineraryDTO>): Promise<ItineraryDTO> {
  return apiPut(`${BASE}/${id}`, body)
}

export function deleteItinerary(id: number): Promise<void> {
  return apiDelete(`${BASE}/${id}`)
}

export function activateItinerary(id: number): Promise<ItineraryDTO> {
  return apiPost(`${BASE}/${id}/activate`)
}

export function completeItinerary(id: number): Promise<ItineraryDTO> {
  return apiPost(`${BASE}/${id}/complete`)
}

export function getItineraryPdfUrl(id: number): string {
  return `${process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8060'}${BASE}/${id}/pdf`
}

// Days
export function getItineraryDays(itineraryId: number): Promise<ItineraryDay[]> {
  return apiGet(`${BASE}/${itineraryId}/days`)
}

export function getItineraryDay(itineraryId: number, dayId: number): Promise<ItineraryDay> {
  return apiGet(`${BASE}/${itineraryId}/days/${dayId}`)
}

export function generateDays(itineraryId: number): Promise<ItineraryDay[]> {
  return apiPost(`${BASE}/${itineraryId}/days/generate`)
}

// Activities
export function getDayActivities(itineraryId: number, dayId: number): Promise<ItineraryActivity[]> {
  return apiGet(`${BASE}/${itineraryId}/activities/day/${dayId}`)
}

export function addActivity(
  itineraryId: number,
  dayId: number,
  body: Partial<ItineraryActivity>
): Promise<ItineraryActivity> {
  return apiPost(`${BASE}/${itineraryId}/activities/day/${dayId}`, body)
}

export function deleteActivity(itineraryId: number, activityId: number): Promise<void> {
  return apiDelete(`${BASE}/${itineraryId}/activities/${activityId}`)
}

// Expenses
export function getExpenses(itineraryId: number): Promise<ItineraryExpense[]> {
  return apiGet(`${BASE}/${itineraryId}/expenses`)
}

export function getExpenseSummary(itineraryId: number): Promise<ExpenseSummary> {
  return apiGet(`${BASE}/${itineraryId}/expenses/summary`)
}

export function addExpense(itineraryId: number, body: CreateExpenseRequest): Promise<ItineraryExpense> {
  return apiPost(`${BASE}/${itineraryId}/expenses`, body)
}

export function deleteExpense(itineraryId: number, expenseId: number): Promise<void> {
  return apiDelete(`${BASE}/${itineraryId}/expenses/${expenseId}`)
}

import { apiGet, apiPost, apiPut, apiDelete, buildQueryString } from './client'
import type {
  EventResponse,
  EventSummary,
  EventAvailability,
  EventTicket,
  CreateEventRequest,
  UpdateEventRequest,
  RegisterForEventRequest,
  EventStatus,
} from '@/types/event'
import type { PaginatedResponse } from '@/types/api'

// Public endpoints (no auth required)
export function getPublicEvents(params?: {
  category?: string
  location?: string
  dateFrom?: string
  dateTo?: string
  minPrice?: number
  maxPrice?: number
  vibe?: string
  authenticCultural?: boolean
  page?: number
  size?: number
}): Promise<PaginatedResponse<EventSummary>> {
  return apiGet(`/api/public/events${buildQueryString(params ?? {})}`)
}

export function getPublicEvent(id: number): Promise<EventResponse> {
  return apiGet(`/api/public/events/${id}`)
}

// Authenticated endpoints
export function getEvents(params?: {
  category?: string
  location?: string
  dateFrom?: string
  dateTo?: string
  minPrice?: number
  maxPrice?: number
  status?: EventStatus
  vibe?: string
  authenticCultural?: boolean
  page?: number
  size?: number
}): Promise<PaginatedResponse<EventSummary>> {
  return apiGet(`/api/events${buildQueryString(params ?? {})}`)
}

export function getEvent(id: number): Promise<{ data: EventResponse }> {
  return apiGet(`/api/events/${id}`)
}

export function checkEventAvailability(id: number): Promise<{ data: EventAvailability }> {
  return apiGet(`/api/events/${id}/availability`)
}

// Tourist - register for event
export function registerForEvent(eventId: number, body: RegisterForEventRequest): Promise<{ data: EventTicket }> {
  return apiPost(`/api/events/${eventId}/registrations`, body)
}

export function cancelTicket(eventId: number, ticketId: string): Promise<void> {
  return apiDelete(`/api/events/${eventId}/registrations/${ticketId}`)
}

export function getMyTickets(): Promise<{ data: EventTicket[] }> {
  return apiGet('/api/events/tickets/my')
}

// Admin endpoints
export function createEvent(body: CreateEventRequest): Promise<{ data: EventResponse }> {
  return apiPost('/api/events', body)
}

export function updateEvent(id: number, body: UpdateEventRequest): Promise<{ data: EventResponse }> {
  return apiPut(`/api/events/${id}`, body)
}

export function deleteEvent(id: number): Promise<{ data: null }> {
  return apiDelete(`/api/events/${id}`)
}

export function getMyAdminEvents(params?: {
  status?: EventStatus
  page?: number
  size?: number
}): Promise<PaginatedResponse<EventSummary>> {
  return apiGet(`/api/events/organizer/my${buildQueryString(params ?? {})}`)
}

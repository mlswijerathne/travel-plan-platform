export type EventStatus = 'DRAFT' | 'PUBLISHED' | 'CANCELLED' | 'COMPLETED'

export interface EventSummary {
  id: number
  title: string
  description: string
  category: string
  location: string
  venueName: string
  startDateTime: string
  endDateTime: string
  ticketPrice: number
  maxCapacity: number
  availableSeats: number
  status: EventStatus
  vibe?: string
  authenticCultural?: boolean
  coverImageUrl?: string
  organizerId: string
}

export interface EventResponse extends EventSummary {
  tags?: string[]
  requirements?: string
  cancellationPolicy?: string
  createdAt: string
  updatedAt: string
}

export interface EventAvailability {
  eventId: number
  totalCapacity: number
  bookedSeats: number
  availableSeats: number
  isAvailable: boolean
}

export interface EventTicket {
  id: number
  ticketReference: string
  eventId: number
  eventTitle: string
  eventStartDateTime: string
  eventLocation: string
  quantity: number
  totalAmount: number
  status: string
  createdAt: string
}

export interface CreateEventRequest {
  title: string
  description: string
  category: string
  location: string
  venueName: string
  startDateTime: string
  endDateTime: string
  ticketPrice: number
  maxCapacity: number
  vibe?: string
  authenticCultural?: boolean
  coverImageUrl?: string
  tags?: string[]
  requirements?: string
  cancellationPolicy?: string
}

export interface UpdateEventRequest extends Partial<CreateEventRequest> {
  status?: EventStatus
}

export interface RegisterForEventRequest {
  quantity: number
}

export const EVENT_CATEGORIES = [
  'CULTURAL',
  'MUSIC',
  'FOOD',
  'ADVENTURE',
  'SPORTS',
  'ARTS',
  'RELIGIOUS',
  'FESTIVAL',
  'WORKSHOP',
  'NETWORKING',
] as const

export const EVENT_VIBES = ['RELAXED', 'ENERGETIC', 'ROMANTIC', 'FAMILY', 'PARTY', 'SPIRITUAL'] as const

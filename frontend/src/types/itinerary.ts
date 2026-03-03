export type ItineraryStatus = 'PLANNING' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED'

export interface ItineraryActivity {
  id: number
  dayId: number
  title: string
  description?: string
  location?: string
  startTime?: string
  endTime?: string
  estimatedCost?: number
  providerType?: string
  providerId?: number
  sortOrder: number
}

export interface ItineraryDay {
  id: number
  itineraryId: number
  dayNumber: number
  date: string
  title?: string
  notes?: string
  activities: ItineraryActivity[]
}

export interface ItineraryExpense {
  id: number
  itineraryId: number
  category: string
  description: string
  amount: number
  currency: string
  date: string
  paidBy?: string
  notes?: string
}

export interface ExpenseSummary {
  totalAmount: number
  currency: string
  byCategory: Record<string, number>
  byDate: Record<string, number>
}

export interface ItineraryDTO {
  id: number
  touristId: string
  title: string
  description?: string
  startDate: string
  endDate: string
  status: ItineraryStatus
  days?: ItineraryDay[]
  totalBudget?: number
  createdAt: string
  updatedAt: string
}

export interface CreateItineraryRequest {
  title: string
  description?: string
  startDate: string
  endDate: string
}

export interface CreateExpenseRequest {
  category: string
  description: string
  amount: number
  currency?: string
  date: string
  paidBy?: string
  notes?: string
}

export const EXPENSE_CATEGORIES = [
  'ACCOMMODATION',
  'TRANSPORT',
  'FOOD',
  'ACTIVITIES',
  'SHOPPING',
  'HEALTH',
  'COMMUNICATION',
  'OTHER',
] as const

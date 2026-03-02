import type { ProviderResult, QuickReplyChip } from './chat'

export interface TripPlanFormData {
  destination: string
  startDate: string
  endDate: string
  budget?: number
  travelers?: number
  interests?: string[]
  travelStyle?: string
  sessionId?: string
}

export interface RecommendationRequest {
  destination: string
  duration?: number
  budget?: number
  interests?: string[]
  travelStyle?: string
}

export interface RecommendationResponse {
  summary: string
  hotels: ProviderResult[]
  tourGuides: ProviderResult[]
  vehicles: ProviderResult[]
  quickReplies?: QuickReplyChip[]
}

export type EntityType = 'HOTEL' | 'TOUR_GUIDE' | 'VEHICLE'

export interface Review {
  id: number
  touristId: string
  entityType: EntityType
  entityId: number
  bookingId: number | null
  rating: number
  title: string | null
  content: string | null
  images: string[]
  isVerified: boolean
  isVisible: boolean
  createdAt: string
  updatedAt: string
  responses: ProviderResponse[]
}

export interface ProviderResponse {
  id: number
  reviewId: number
  providerId: string
  content: string
  createdAt: string
}

export interface ReviewSummary {
  entityType: EntityType
  entityId: number
  averageRating: number
  reviewCount: number
  fiveStarCount: number
  fourStarCount: number
  threeStarCount: number
  twoStarCount: number
  oneStarCount: number
}

export interface PendingReview {
  id: number
  touristId: string
  entityType: EntityType
  entityId: number
  entityName: string
  bookingId: number
  tripEndDate: string
  isCompleted: boolean
  createdAt: string
}

export interface CreateReviewRequest {
  entityType: EntityType
  entityId: number
  bookingId?: number
  rating: number
  title?: string
  content?: string
  images?: string[]
}

export interface UpdateReviewRequest {
  rating?: number
  title?: string
  content?: string
  images?: string[]
}

export interface ProviderResponseRequest {
  content: string
}

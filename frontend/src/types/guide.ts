export interface Guide {
  id: number
  userId: string
  firstName: string
  lastName: string
  email: string
  phoneNumber: string | null
  bio: string | null
  languages: string[]
  specializations: string[]
  experienceYears: number | null
  hourlyRate: number
  dailyRate: number
  averageRating: number
  reviewCount: number
  profileImageUrl: string | null
  isVerified: boolean
  isActive: boolean
  createdAt: string
  updatedAt: string
}

export interface GuideAvailability {
  guideId: number
  available: boolean
  dailyRate: number
  bookedDates: string[]
}

export interface GuideCreateRequest {
  firstName: string
  lastName: string
  email: string
  phoneNumber?: string
  bio?: string
  languages?: string[]
  specializations?: string[]
  experienceYears?: number
  hourlyRate: number
  dailyRate: number
  profileImageUrl?: string
}

export type GuideUpdateRequest = GuideCreateRequest

export const GUIDE_SPECIALIZATIONS = [
  'wildlife', 'cultural', 'adventure', 'historical', 'food_tours',
  'bird_watching', 'national_parks', 'diving', 'surfing', 'hiking',
] as const

export const GUIDE_LANGUAGES = [
  'English', 'Sinhala', 'Tamil', 'French', 'German',
  'Japanese', 'Chinese', 'Spanish', 'Italian', 'Russian',
] as const

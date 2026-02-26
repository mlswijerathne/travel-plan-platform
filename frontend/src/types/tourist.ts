export interface Tourist {
  id: number
  userId: string
  email: string
  firstName: string
  lastName: string
  phoneNumber: string | null
  nationality: string | null
  profileImageUrl: string | null
  isActive: boolean
  preferences: TouristPreferences | null
  createdAt: string
  updatedAt: string
}

export interface TouristPreferences {
  preferredBudget: string | null
  travelStyle: string | null
  dietaryRestrictions: string[]
  interests: string[]
  preferredLanguages: string[]
  accessibilityNeeds: string | null
}

export interface TouristRegistrationRequest {
  userId: string
  email: string
  firstName: string
  lastName: string
  phoneNumber?: string
  nationality?: string
  profileImageUrl?: string
  preferredBudget?: string
}

export interface TouristUpdateRequest {
  firstName?: string
  lastName?: string
  phoneNumber?: string
  nationality?: string
  profileImageUrl?: string
}

export interface PreferenceRequest {
  preferredBudget?: string
  travelStyle?: string
  dietaryRestrictions?: string[]
  interests?: string[]
  preferredLanguages?: string[]
  accessibilityNeeds?: string
}

export interface WalletResponse {
  balance: number
  transactions: WalletTransaction[]
}

export interface WalletTransaction {
  id: number
  amount: number
  type: string
  description: string
  referenceId: string | null
  createdAt: string
}

export interface ApiResponse<T> {
  data: T
  meta: {
    timestamp: string
    requestId: string | null
  }
}

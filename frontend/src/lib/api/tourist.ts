import { apiGet, apiPost, apiPut } from './client'
import type {
  ApiResponse,
  Tourist,
  TouristRegistrationRequest,
  TouristUpdateRequest,
  TouristPreferences,
  PreferenceRequest,
  WalletResponse,
} from '@/types/tourist'

export async function registerTourist(data: TouristRegistrationRequest): Promise<ApiResponse<Tourist>> {
  return apiPost<ApiResponse<Tourist>>('/api/tourists/register', data)
}

export async function getCurrentTourist(): Promise<ApiResponse<Tourist>> {
  return apiGet<ApiResponse<Tourist>>('/api/tourists/me')
}

export async function updateTouristProfile(data: TouristUpdateRequest): Promise<ApiResponse<Tourist>> {
  return apiPut<ApiResponse<Tourist>>('/api/tourists/me', data)
}

export async function getTouristPreferences(): Promise<ApiResponse<TouristPreferences>> {
  return apiGet<ApiResponse<TouristPreferences>>('/api/tourists/me/preferences')
}

export async function updateTouristPreferences(data: PreferenceRequest): Promise<ApiResponse<TouristPreferences>> {
  return apiPut<ApiResponse<TouristPreferences>>('/api/tourists/me/preferences', data)
}

export async function getTouristWallet(): Promise<ApiResponse<WalletResponse>> {
  return apiGet<ApiResponse<WalletResponse>>('/api/tourists/me/wallet')
}

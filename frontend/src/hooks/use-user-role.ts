'use client'

import { useEffect, useState } from 'react'
import { createClient } from '@/lib/supabase/client'

export type UserRole = 'TOURIST' | 'HOTEL_OWNER' | 'TOUR_GUIDE' | 'VEHICLE_OWNER' | 'ADMIN'

export function useUserRole() {
  const [role, setRole] = useState<UserRole>('TOURIST')
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    async function detectRole() {
      try {
        const supabase = createClient()
        const { data: { session } } = await supabase.auth.getSession()

        if (session?.access_token) {
          const payload = JSON.parse(atob(session.access_token.split('.')[1]))
          const userRole = payload.user_metadata?.role || payload.app_metadata?.role || 'TOURIST'
          setRole(userRole as UserRole)
        }
      } catch {
        setRole('TOURIST')
      } finally {
        setIsLoading(false)
      }
    }

    detectRole()
  }, [])

  const isProvider = role === 'HOTEL_OWNER' || role === 'TOUR_GUIDE' || role === 'VEHICLE_OWNER'
  const isAdmin = role === 'ADMIN'
  const isTourist = role === 'TOURIST'

  return { role, isProvider, isAdmin, isTourist, isLoading }
}

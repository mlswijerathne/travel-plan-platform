'use client'

import { useEffect, useState } from 'react'
import { createClient } from '@/lib/supabase/client'

export function useCurrentUser() {
  const [displayName, setDisplayName] = useState<string>('')

  useEffect(() => {
    async function load() {
      try {
        const supabase = createClient()
        const { data: { session } } = await supabase.auth.getSession()
        if (session?.access_token) {
          const payload = JSON.parse(atob(session.access_token.split('.')[1]))
          const name = payload.user_metadata?.full_name || payload.email || ''
          setDisplayName(name)
        }
      } catch { /* ignore */ }
    }
    load()
  }, [])

  return { displayName }
}

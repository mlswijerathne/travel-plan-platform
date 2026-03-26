import type { Metadata } from 'next'
import { createClient } from '@/lib/supabase/server'
import { redirect } from 'next/navigation'
import { AuthNavClient } from '@/components/shared/AuthNavClient'

export const metadata: Metadata = {
  title: {
    template: '%s | TravelPlan',
    default: 'Explore Sri Lanka | TravelPlan',
  },
  description: 'Discover hotels, tour guides, vehicles, and events across Sri Lanka. Plan your perfect trip with our AI travel assistant.',
}

export default async function TouristLayout({
  children,
}: {
  children: React.ReactNode
}) {
  const supabase = await createClient()
  const { data: { user } } = await supabase.auth.getUser()

  if (!user) {
    redirect('/login')
  }

  return (
    <div className="min-h-screen bg-background">
      <AuthNavClient userEmail={user.email ?? ''} />
      <main className="max-w-6xl mx-auto px-4 sm:px-6 py-6 sm:py-8">
        {children}
      </main>
    </div>
  )
}

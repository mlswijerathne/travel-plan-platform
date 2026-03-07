import { createClient } from '@/lib/supabase/server'
import { redirect } from 'next/navigation'
import { AuthNavClient } from '@/components/shared/AuthNavClient'

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
    <div className="min-h-screen bg-gradient-to-b from-teal-50/30 to-white">
      <AuthNavClient userEmail={user.email ?? ''} />
      <main className="max-w-6xl mx-auto px-4 sm:px-6 py-6 sm:py-8">
        {children}
      </main>
    </div>
  )
}

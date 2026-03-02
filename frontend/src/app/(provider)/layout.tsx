import { createClient } from '@/lib/supabase/server'
import { redirect } from 'next/navigation'
import { AuthNav } from '@/components/shared/AuthNav'

export default async function ProviderLayout({
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
    <div className="min-h-screen bg-neutral-50">
      <AuthNav userEmail={user.email ?? ''} />
      <main className="max-w-6xl mx-auto px-4 sm:px-6 py-6 sm:py-8">
        {children}
      </main>
    </div>
  )
}

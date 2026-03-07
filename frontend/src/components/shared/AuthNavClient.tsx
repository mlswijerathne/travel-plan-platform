'use client'

import dynamic from 'next/dynamic'

const AuthNav = dynamic(
  () => import('@/components/shared/AuthNav').then(m => ({ default: m.AuthNav })),
  {
    ssr: false,
    loading: () => (
      <div className="sticky top-0 z-50 h-14 border-b border-border/50 bg-white/80" />
    ),
  }
)

export function AuthNavClient({ userEmail }: { userEmail: string }) {
  return <AuthNav userEmail={userEmail} />
}

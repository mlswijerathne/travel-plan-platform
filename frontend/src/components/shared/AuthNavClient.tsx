'use client'

import dynamic from 'next/dynamic'

const AuthNav = dynamic(
  () => import('@/components/shared/AuthNav').then(m => ({ default: m.AuthNav })),
  {
    ssr: false,
    loading: () => (
      <div className="sticky top-0 z-50 h-16 bg-white/80 shadow-[0_12px_40px_rgba(23,29,28,0.06)]" />
    ),
  }
)

export function AuthNavClient({ userEmail }: { userEmail: string }) {
  return <AuthNav userEmail={userEmail} />
}

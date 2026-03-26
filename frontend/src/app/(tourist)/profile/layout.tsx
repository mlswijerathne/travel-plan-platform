import type { Metadata } from 'next'

export const metadata: Metadata = {
  title: 'Profile',
  description: 'Manage your TravelPlan profile, travel preferences, and account settings.',
}

export default function Layout({ children }: { children: React.ReactNode }) {
  return <>{children}</>
}

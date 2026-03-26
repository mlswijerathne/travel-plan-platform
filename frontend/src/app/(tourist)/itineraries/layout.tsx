import type { Metadata } from 'next'

export const metadata: Metadata = {
  title: 'My Itineraries',
  description: 'View and manage your Sri Lanka travel itineraries. Track expenses, activities, and trip details.',
}

export default function Layout({ children }: { children: React.ReactNode }) {
  return <>{children}</>
}

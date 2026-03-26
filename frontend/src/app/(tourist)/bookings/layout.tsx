import type { Metadata } from 'next'

export const metadata: Metadata = {
  title: 'My Bookings',
  description: 'Manage your hotel, guide, and vehicle bookings for Sri Lanka. Track confirmations and trip details.',
}

export default function Layout({ children }: { children: React.ReactNode }) {
  return <>{children}</>
}

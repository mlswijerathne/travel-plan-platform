import type { Metadata } from 'next'

export const metadata: Metadata = {
  title: 'Wallet',
  description: 'Manage your TravelPlan wallet balance, view transactions, and track travel spending.',
}

export default function Layout({ children }: { children: React.ReactNode }) {
  return <>{children}</>
}

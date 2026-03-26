import type { Metadata } from 'next'

export const metadata: Metadata = {
  title: 'Create Account',
  description: 'Join TravelPlan as a tourist or service provider. Plan AI-powered Sri Lanka trips, or list your hotel, tour guide services, or vehicles to reach travelers worldwide.',
}

export default function RegisterLayout({ children }: { children: React.ReactNode }) {
  return <>{children}</>
}

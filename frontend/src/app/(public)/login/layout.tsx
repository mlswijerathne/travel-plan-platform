import type { Metadata } from 'next'

export const metadata: Metadata = {
  title: 'Sign In',
  description: 'Sign in to TravelPlan to continue planning your Sri Lanka adventure. Access your itineraries, bookings, and AI travel assistant.',
}

export default function LoginLayout({ children }: { children: React.ReactNode }) {
  return <>{children}</>
}

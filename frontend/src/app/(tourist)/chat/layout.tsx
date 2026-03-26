import type { Metadata } from 'next'

export const metadata: Metadata = {
  title: 'AI Trip Planner',
  description: 'Chat with our AI travel assistant to plan your perfect Sri Lanka itinerary. Personalized recommendations in minutes.',
}

export default function Layout({ children }: { children: React.ReactNode }) {
  return <>{children}</>
}

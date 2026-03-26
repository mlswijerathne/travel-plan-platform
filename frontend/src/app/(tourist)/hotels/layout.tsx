import type { Metadata } from 'next'

export const metadata: Metadata = {
  title: 'Hotels in Sri Lanka',
  description: 'Browse verified hotels across Sri Lanka. Compare prices, read reviews, and book your perfect stay.',
}

export default function Layout({ children }: { children: React.ReactNode }) {
  return <>{children}</>
}

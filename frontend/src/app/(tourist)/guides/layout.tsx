import type { Metadata } from 'next'

export const metadata: Metadata = {
  title: 'Tour Guides',
  description: 'Find experienced local tour guides in Sri Lanka. Explore their specializations, reviews, and availability.',
}

export default function Layout({ children }: { children: React.ReactNode }) {
  return <>{children}</>
}

import type { Metadata } from 'next'

export const metadata: Metadata = {
  title: 'Events & Activities',
  description: 'Discover cultural events, festivals, and activities happening across Sri Lanka. Book unique experiences.',
}

export default function Layout({ children }: { children: React.ReactNode }) {
  return <>{children}</>
}

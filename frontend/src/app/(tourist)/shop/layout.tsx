import type { Metadata } from 'next'

export const metadata: Metadata = {
  title: 'Travel Shop',
  description: 'Browse Sri Lankan souvenirs and travel essentials. Shop unique local products delivered to your hotel.',
}

export default function Layout({ children }: { children: React.ReactNode }) {
  return <>{children}</>
}

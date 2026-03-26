import type { Metadata } from 'next'

export const metadata: Metadata = {
  title: 'Vehicle Rental',
  description: 'Rent cars, vans, and tuk-tuks for your Sri Lanka trip. Browse available vehicles from verified local owners.',
}

export default function Layout({ children }: { children: React.ReactNode }) {
  return <>{children}</>
}

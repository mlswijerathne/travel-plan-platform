import type { Metadata, Viewport } from 'next'
import { Inter, Plus_Jakarta_Sans } from 'next/font/google'
import './globals.css'
import { Providers } from '@/providers'
import { Toaster } from '@/components/ui/sonner'

const inter = Inter({
  subsets: ['latin'],
  variable: '--font-body',
})

const jakartaSans = Plus_Jakarta_Sans({
  subsets: ['latin'],
  variable: '--font-display',
  weight: ['500', '600', '700', '800'],
})

export const viewport: Viewport = {
  themeColor: '#00685f',
  width: 'device-width',
  initialScale: 1,
}

export const metadata: Metadata = {
  title: {
    default: 'TravelPlan — AI-Powered Trip Planning for Sri Lanka',
    template: '%s | TravelPlan',
  },
  description:
    'Plan your perfect Sri Lanka adventure with AI-powered recommendations. Chat naturally, get personalized itineraries with the best local hotels, tour guides, and vehicles — then book everything in one click.',
  keywords: [
    'Sri Lanka travel',
    'AI trip planner',
    'Sri Lanka hotels',
    'Sri Lanka tour guides',
    'Sri Lanka vehicle rental',
    'travel itinerary',
    'Sri Lanka tourism',
    'personalized travel',
    'book Sri Lanka trip',
    'Ella Sri Lanka',
    'Galle Sri Lanka',
    'Sigiriya Sri Lanka',
    'Kandy travel',
    'Colombo travel',
    'Sri Lanka holiday',
    'budget travel Sri Lanka',
    'luxury travel Sri Lanka',
  ],
  authors: [{ name: 'TravelPlan' }],
  creator: 'TravelPlan',
  publisher: 'TravelPlan',
  applicationName: 'TravelPlan',
  category: 'Travel',
  robots: {
    index: true,
    follow: true,
    googleBot: {
      index: true,
      follow: true,
      'max-video-preview': -1,
      'max-image-preview': 'large',
      'max-snippet': -1,
    },
  },
  openGraph: {
    type: 'website',
    locale: 'en_US',
    siteName: 'TravelPlan',
    title: 'TravelPlan — AI-Powered Trip Planning for Sri Lanka',
    description:
      'Plan your perfect Sri Lanka adventure with AI. Chat naturally, get personalized itineraries, and book hotels, guides & transport in one click.',
  },
  twitter: {
    card: 'summary_large_image',
    title: 'TravelPlan — AI-Powered Trip Planning for Sri Lanka',
    description:
      'Plan your perfect Sri Lanka adventure with AI. Personalized itineraries, verified local providers, one-click booking.',
    creator: '@travelplan',
  },
  alternates: {
    canonical: '/',
  },
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en" suppressHydrationWarning>
      <head>
        <link rel="icon" href="/favicon.ico" sizes="any" />
        <link rel="apple-touch-icon" href="/apple-touch-icon.png" />
      </head>
      <body className={`${inter.variable} ${jakartaSans.variable} ${inter.className}`}>
        <Providers>
          {children}
          <Toaster richColors position="top-right" />
        </Providers>
      </body>
    </html>
  )
}

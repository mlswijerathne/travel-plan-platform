import Link from 'next/link'
import { Compass } from 'lucide-react'

export default function NotFound() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-b from-teal-50/30 to-white px-4">
      <div className="text-center max-w-md">
        <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-primary/10 mb-6">
          <Compass className="h-8 w-8 text-primary" />
        </div>
        <h1 className="text-6xl font-extrabold text-foreground mb-2">404</h1>
        <h2 className="text-xl font-semibold text-foreground mb-3">Page Not Found</h2>
        <p className="text-muted-foreground mb-8">
          The page you&apos;re looking for doesn&apos;t exist or has been moved.
        </p>
        <div className="flex items-center justify-center gap-3">
          <Link
            href="/"
            className="inline-flex items-center justify-center px-6 py-2.5 rounded-lg bg-primary text-white font-medium text-sm hover:bg-primary/90 transition-colors"
          >
            Go Home
          </Link>
          <Link
            href="/chat"
            className="inline-flex items-center justify-center px-6 py-2.5 rounded-lg border text-sm font-medium text-foreground hover:bg-accent transition-colors"
          >
            Plan a Trip
          </Link>
        </div>
      </div>
    </div>
  )
}

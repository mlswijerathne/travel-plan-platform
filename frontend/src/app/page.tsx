import Link from 'next/link'

export default function HomePage() {
  return (
    <main className="min-h-screen flex flex-col items-center justify-center p-8">
      <div className="max-w-4xl text-center">
        <h1 className="text-5xl font-bold mb-6 bg-gradient-to-r from-primary to-green-400 bg-clip-text text-transparent">
          Travel Plan Sri Lanka
        </h1>
        <p className="text-xl text-muted-foreground mb-8">
          Plan your perfect adventure with AI-powered recommendations.
          Discover hotels, tour guides, and vehicles tailored to your preferences.
        </p>
        <div className="flex gap-4 justify-center">
          <Link
            href="/login"
            className="px-6 py-3 bg-primary text-primary-foreground rounded-lg font-medium hover:opacity-90 transition"
          >
            Get Started
          </Link>
          <Link
            href="/packages"
            className="px-6 py-3 border border-border rounded-lg font-medium hover:bg-accent transition"
          >
            Browse Packages
          </Link>
        </div>
      </div>
    </main>
  )
}

import Link from 'next/link'
import { Button } from '@/components/ui/button'
import {
  MessageSquare,
  Map,
  ShieldCheck,
  Wallet,
  ArrowRight,
  Compass,
  Star,
  Users,
  Building2,
  Send,
  ChevronRight,
  Globe,
  MapPin,
  Car,
} from 'lucide-react'

const FEATURES = [
  {
    icon: MessageSquare,
    title: 'AI Trip Planning',
    description: 'Chat naturally with our AI assistant. Describe your dream trip and get a personalized itinerary in minutes.',
    color: 'bg-teal-50 text-teal-600',
  },
  {
    icon: Map,
    title: 'Smart Itineraries',
    description: 'Day-by-day schedules with map visualization, cost breakdowns, and real-time weather insights.',
    color: 'bg-orange-50 text-orange-500',
  },
  {
    icon: ShieldCheck,
    title: 'Trusted Providers',
    description: 'Verified local hotels, tour guides, and vehicle owners. Real reviews from real travelers.',
    color: 'bg-emerald-50 text-emerald-600',
  },
  {
    icon: Wallet,
    title: 'Budget Control',
    description: 'Set your budget and let AI optimize your trip. Track expenses and manage wallet credits effortlessly.',
    color: 'bg-blue-50 text-blue-500',
  },
]

const STEPS = [
  {
    step: '01',
    title: 'Tell Us Your Dream',
    description: 'Chat with our AI about where you want to go, when, and what excites you. No forms to fill.',
    icon: MessageSquare,
  },
  {
    step: '02',
    title: 'Get Your Plan',
    description: 'Receive a personalized itinerary with the best hotels, guides, and transport — all within your budget.',
    icon: Compass,
  },
  {
    step: '03',
    title: 'Book & Go',
    description: 'Book everything in one click. Download your itinerary and enjoy a stress-free adventure.',
    icon: Send,
  },
]

const DESTINATIONS = [
  { name: 'Colombo', tagline: 'Capital Vibes', gradient: 'from-teal-600/80 to-teal-900/80' },
  { name: 'Kandy', tagline: 'Cultural Heart', gradient: 'from-emerald-600/80 to-emerald-900/80' },
  { name: 'Galle', tagline: 'Coastal Charm', gradient: 'from-blue-600/80 to-blue-900/80' },
  { name: 'Ella', tagline: 'Mountain Magic', gradient: 'from-violet-600/80 to-violet-900/80' },
  { name: 'Sigiriya', tagline: 'Ancient Wonder', gradient: 'from-amber-600/80 to-amber-900/80' },
  { name: 'Mirissa', tagline: 'Beach Paradise', gradient: 'from-cyan-600/80 to-cyan-900/80' },
]

const STATS = [
  { value: '100+', label: 'Hotels', icon: Building2 },
  { value: '50+', label: 'Tour Guides', icon: Users },
  { value: '4.8', label: 'Avg Rating', icon: Star },
  { value: '1K+', label: 'Trips Planned', icon: Globe },
]

const PROVIDER_TYPES = [
  {
    icon: Building2,
    title: 'Hotel Owners',
    description: 'List your property on TravelPlan and reach thousands of travelers visiting Sri Lanka. Manage bookings, rooms, and reviews all in one place.',
    cta: 'List Your Property',
    href: '/register?role=HOTEL_OWNER',
    color: 'bg-blue-50 text-blue-600',
  },
  {
    icon: MapPin,
    title: 'Tour Guides',
    description: 'Share your local expertise with travelers from around the world. Set your own rates, manage your schedule, and grow your reputation.',
    cta: 'Become a Guide',
    href: '/register?role=TOUR_GUIDE',
    color: 'bg-emerald-50 text-emerald-600',
  },
  {
    icon: Car,
    title: 'Vehicle Owners',
    description: 'Rent your vehicles to tourists and earn. From tuk-tuks to luxury cars, reach travelers looking for reliable transport.',
    cta: 'Register Your Vehicle',
    href: '/register?role=VEHICLE_OWNER',
    color: 'bg-orange-50 text-orange-500',
  },
]

export default function HomePage() {
  return (
    <main className="min-h-screen">
      {/* Navigation */}
      <nav className="sticky top-0 z-50 border-b border-border/50 bg-white/80 backdrop-blur-xl">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <Link href="/" className="flex items-center gap-2">
            <div className="h-8 w-8 rounded-lg bg-primary flex items-center justify-center">
              <Compass className="h-5 w-5 text-white" />
            </div>
            <span className="font-display text-xl font-bold text-foreground">
              Travel<span className="text-primary">Plan</span>
            </span>
          </Link>

          <div className="hidden md:flex items-center gap-8">
            <a href="#features" className="text-sm font-medium text-muted-foreground hover:text-foreground transition-colors">
              Features
            </a>
            <a href="#how-it-works" className="text-sm font-medium text-muted-foreground hover:text-foreground transition-colors">
              How It Works
            </a>
            <a href="#destinations" className="text-sm font-medium text-muted-foreground hover:text-foreground transition-colors">
              Destinations
            </a>
            <a href="#providers" className="text-sm font-medium text-muted-foreground hover:text-foreground transition-colors">
              For Providers
            </a>
          </div>

          <div className="flex items-center gap-3">
            <Button asChild variant="ghost" size="sm" className="hidden sm:inline-flex">
              <Link href="/login">Sign In</Link>
            </Button>
            <Button asChild size="sm" className="rounded-full px-5">
              <Link href="/register">
                Get Started
                <ArrowRight className="ml-1 h-4 w-4" />
              </Link>
            </Button>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="relative overflow-hidden">
        {/* Background decorations */}
        <div className="absolute inset-0 bg-gradient-to-b from-teal-50/50 via-white to-white" />
        <div className="absolute top-0 left-1/2 -translate-x-1/2 w-[800px] h-[800px] bg-gradient-to-br from-teal-100/40 via-transparent to-orange-100/30 rounded-full blur-3xl" />
        <div className="absolute top-40 -left-40 w-80 h-80 bg-teal-100/50 rounded-full blur-3xl" />
        <div className="absolute top-60 -right-40 w-80 h-80 bg-orange-100/40 rounded-full blur-3xl" />

        <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-20 pb-24 sm:pt-28 sm:pb-32">
          <div className="max-w-4xl mx-auto text-center">
            <div className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full border border-primary/20 bg-primary/5 text-primary text-sm font-medium mb-8">
              <span className="relative flex h-2 w-2">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-primary opacity-75"></span>
                <span className="relative inline-flex rounded-full h-2 w-2 bg-primary"></span>
              </span>
              AI-Powered Travel Planning
            </div>

            <h1 className="font-display text-5xl sm:text-6xl lg:text-7xl font-extrabold tracking-tight leading-[1.1]">
              Your AI Travel{' '}
              <br className="hidden sm:block" />
              Companion for{' '}
              <span className="bg-gradient-to-r from-primary via-teal-500 to-emerald-400 bg-clip-text text-transparent">
                Sri Lanka
              </span>
            </h1>

            <p className="mt-6 text-lg sm:text-xl text-muted-foreground max-w-2xl mx-auto leading-relaxed">
              Plan your perfect adventure in minutes. Chat with our AI, get personalized
              itineraries with the best local hotels, guides, and transport — then book everything in one click.
            </p>

            <div className="mt-10 flex flex-col sm:flex-row gap-4 justify-center">
              <Button asChild size="lg" className="text-base px-8 rounded-full h-12 shadow-lg shadow-primary/25">
                <Link href="/register">
                  Start Planning Free
                  <ArrowRight className="ml-2 h-5 w-5" />
                </Link>
              </Button>
              <Button asChild variant="outline" size="lg" className="text-base px-8 rounded-full h-12">
                <Link href="/login">
                  Sign In
                </Link>
              </Button>
            </div>

            {/* Stats */}
            <div className="mt-16 grid grid-cols-2 sm:grid-cols-4 gap-6 max-w-2xl mx-auto">
              {STATS.map(({ value, label, icon: Icon }) => (
                <div key={label} className="text-center">
                  <div className="inline-flex items-center justify-center w-10 h-10 rounded-full bg-primary/10 mb-2">
                    <Icon className="h-5 w-5 text-primary" />
                  </div>
                  <div className="font-display text-2xl font-bold text-foreground">{value}</div>
                  <div className="text-sm text-muted-foreground">{label}</div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section id="features" className="py-24 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-2xl mx-auto mb-16">
            <p className="text-sm font-semibold text-primary uppercase tracking-wider mb-3">Why TravelPlan</p>
            <h2 className="font-display text-3xl sm:text-4xl font-bold text-foreground">
              Everything you need for the perfect trip
            </h2>
            <p className="mt-4 text-muted-foreground text-lg">
              From AI-powered planning to real-time budget tracking, we handle the details so you can focus on the adventure.
            </p>
          </div>

          <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {FEATURES.map(({ icon: Icon, title, description, color }) => (
              <div
                key={title}
                className="group relative p-6 rounded-2xl border border-border/50 bg-white hover:shadow-lg hover:shadow-primary/5 transition-all duration-300 hover:-translate-y-1"
              >
                <div className={`inline-flex items-center justify-center w-12 h-12 rounded-xl ${color} mb-4`}>
                  <Icon className="h-6 w-6" />
                </div>
                <h3 className="font-display text-lg font-semibold text-foreground mb-2">{title}</h3>
                <p className="text-sm text-muted-foreground leading-relaxed">{description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* How It Works */}
      <section id="how-it-works" className="py-24 bg-gradient-to-b from-teal-50/30 to-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-2xl mx-auto mb-16">
            <p className="text-sm font-semibold text-primary uppercase tracking-wider mb-3">How It Works</p>
            <h2 className="font-display text-3xl sm:text-4xl font-bold text-foreground">
              Three steps to your dream trip
            </h2>
          </div>

          <div className="grid md:grid-cols-3 gap-8 lg:gap-12 relative">
            {/* Connecting line (desktop only) */}
            <div className="hidden md:block absolute top-16 left-[20%] right-[20%] h-0.5 bg-gradient-to-r from-primary/20 via-primary/40 to-primary/20" />

            {STEPS.map(({ step, title, description, icon: Icon }) => (
              <div key={step} className="relative text-center">
                <div className="relative inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-primary text-white mb-6 shadow-lg shadow-primary/25">
                  <Icon className="h-7 w-7" />
                  <span className="absolute -top-2 -right-2 w-7 h-7 rounded-full bg-coral text-white text-xs font-bold flex items-center justify-center shadow-sm">
                    {step}
                  </span>
                </div>
                <h3 className="font-display text-xl font-semibold text-foreground mb-3">{title}</h3>
                <p className="text-muted-foreground leading-relaxed max-w-sm mx-auto">{description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Destinations */}
      <section id="destinations" className="py-24 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-2xl mx-auto mb-16">
            <p className="text-sm font-semibold text-primary uppercase tracking-wider mb-3">Explore Sri Lanka</p>
            <h2 className="font-display text-3xl sm:text-4xl font-bold text-foreground">
              Popular destinations waiting for you
            </h2>
            <p className="mt-4 text-muted-foreground text-lg">
              From ancient temples to pristine beaches, discover the wonders of the Pearl of the Indian Ocean.
            </p>
          </div>

          <div className="grid grid-cols-2 md:grid-cols-3 gap-4 lg:gap-6">
            {DESTINATIONS.map(({ name, tagline, gradient }, i) => (
              <Link
                key={name}
                href="/register"
                className={`group relative overflow-hidden rounded-2xl bg-muted ${i < 2 ? 'md:col-span-1 aspect-[4/3]' : 'aspect-[4/3]'} transition-all duration-300 hover:shadow-xl hover:-translate-y-1`}
              >
                <div className={`absolute inset-0 bg-gradient-to-br ${gradient}`} />
                <div className="absolute inset-0 bg-gradient-to-t from-black/40 to-transparent" />
                <div className="relative h-full flex flex-col justify-end p-5 sm:p-6">
                  <p className="text-white/80 text-xs sm:text-sm font-medium">{tagline}</p>
                  <h3 className="font-display text-xl sm:text-2xl font-bold text-white">{name}</h3>
                  <div className="mt-2 inline-flex items-center text-white/80 text-sm opacity-0 group-hover:opacity-100 transition-opacity">
                    Explore <ChevronRight className="h-4 w-4 ml-1" />
                  </div>
                </div>
              </Link>
            ))}
          </div>
        </div>
      </section>

      {/* For Providers */}
      <section id="providers" className="py-24 bg-gradient-to-b from-white to-teal-50/30">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-2xl mx-auto mb-16">
            <p className="text-sm font-semibold text-primary uppercase tracking-wider mb-3">For Providers</p>
            <h2 className="font-display text-3xl sm:text-4xl font-bold text-foreground">
              Grow your business with TravelPlan
            </h2>
            <p className="mt-4 text-muted-foreground text-lg">
              Join our network of verified providers and connect with travelers from around the world.
            </p>
          </div>

          <div className="grid md:grid-cols-3 gap-6 lg:gap-8">
            {PROVIDER_TYPES.map(({ icon: Icon, title, description, cta, href, color }) => (
              <div
                key={title}
                className="group relative p-8 rounded-2xl border border-border/50 bg-white hover:shadow-lg hover:shadow-primary/5 transition-all duration-300 hover:-translate-y-1"
              >
                <div className={`inline-flex items-center justify-center w-14 h-14 rounded-xl ${color} mb-5`}>
                  <Icon className="h-7 w-7" />
                </div>
                <h3 className="font-display text-xl font-semibold text-foreground mb-3">{title}</h3>
                <p className="text-muted-foreground leading-relaxed mb-6">{description}</p>
                <Button asChild variant="outline" className="rounded-full">
                  <Link href={href}>
                    {cta}
                    <ArrowRight className="ml-2 h-4 w-4" />
                  </Link>
                </Button>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-24 relative overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-br from-primary via-teal-600 to-emerald-600" />
        <div className="absolute top-0 right-0 w-96 h-96 bg-white/5 rounded-full blur-3xl" />
        <div className="absolute bottom-0 left-0 w-96 h-96 bg-white/5 rounded-full blur-3xl" />

        <div className="relative max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h2 className="font-display text-3xl sm:text-4xl lg:text-5xl font-bold text-white leading-tight">
            Ready to plan your
            <br />Sri Lanka adventure?
          </h2>
          <p className="mt-6 text-lg text-white/80 max-w-xl mx-auto">
            Join thousands of travelers who planned their perfect trip with our AI companion. It takes less than 5 minutes.
          </p>
          <div className="mt-10 flex flex-col sm:flex-row gap-4 justify-center">
            <Button asChild size="lg" className="bg-white text-primary hover:bg-white/90 text-base px-8 rounded-full h-12 shadow-lg">
              <Link href="/register">
                Get Started Free
                <ArrowRight className="ml-2 h-5 w-5" />
              </Link>
            </Button>
            <Button asChild variant="outline" size="lg" className="border-white/30 text-white hover:bg-white/10 text-base px-8 rounded-full h-12">
              <Link href="/login">
                I have an account
              </Link>
            </Button>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-border bg-muted/30">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
          <div className="flex flex-col md:flex-row justify-between items-center gap-6">
            <div className="flex items-center gap-2">
              <div className="h-7 w-7 rounded-lg bg-primary flex items-center justify-center">
                <Compass className="h-4 w-4 text-white" />
              </div>
              <span className="font-display text-lg font-bold">
                Travel<span className="text-primary">Plan</span>
              </span>
            </div>

            <div className="flex flex-wrap items-center justify-center gap-x-6 gap-y-2 text-sm text-muted-foreground">
              <a href="#features" className="hover:text-foreground transition-colors">Features</a>
              <a href="#how-it-works" className="hover:text-foreground transition-colors">How It Works</a>
              <a href="#destinations" className="hover:text-foreground transition-colors">Destinations</a>
              <Link href="/register?role=HOTEL_OWNER" className="hover:text-foreground transition-colors">List Property</Link>
              <Link href="/register?role=TOUR_GUIDE" className="hover:text-foreground transition-colors">Become a Guide</Link>
              <Link href="/login" className="hover:text-foreground transition-colors">Sign In</Link>
            </div>

            <p className="text-sm text-muted-foreground">
              &copy; {new Date().getFullYear()} TravelPlan. All rights reserved.
            </p>
          </div>
        </div>
      </footer>
    </main>
  )
}

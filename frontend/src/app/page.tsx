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
  Sparkles,
  Zap,
} from 'lucide-react'

const FEATURES = [
  {
    icon: MessageSquare,
    title: 'Natural Conversation',
    description: 'Forget rigid filters. Speak naturally about how you want to feel and what you wish to see.',
    image: 'https://lh3.googleusercontent.com/aida-public/AB6AXuAtcDWV2s6dOsU8cszjkIXPFHnUpIBUoqISE8Cz3rjjx8J-bbAYHZatWenm8J_bS-jLDdiV6GyEVzhlSyiVBl8EndqqpA9tsFhuGJNpniEz9UW0b0IfP9ny63ksOx5mGH27aezphBzEohneS6AjtCVvXXQbHZkIyYWNcwiHWpOfyLYyNp42ymrF3UdPYQugyHGSVc_-k4Jshy4dd9WpbPWaL3Drm9AWllEkU9GuM-vlaKr8VevbMckYRhs7flAU_QsLeoG7-wNT_qI',
  },
  {
    icon: Map,
    title: 'Instant Itineraries',
    description: 'Real-time mapping that respects seasonal nuances and elusive local secrets only experts know.',
    image: 'https://lh3.googleusercontent.com/aida-public/AB6AXuCR-Ol1rRSwm1PfDtaLtWtFEFJY2Ot4Nw4r-YpIJNAxpiRD3xqqG5wGxf8S5l8ZiryvHT-MzjiAzG2WRDan9AJy5t8CHbNrhRfskyW9f8xmXs3NGKnU63siQKLb2Z_M5CskzGj-QEjwzkyLLDRcx1XnpSdoD3feC-uTN-fnzdwQe-wTSyqy1BUMkltTbY4nkPKRKrFaRzkNdwZOXbJlzRhNwV518z94Y6mkTGTW_IlUMelpcvuARsC_UY_cWfmxZJFTpJbrVTNZwBU',
  },
  {
    icon: ShieldCheck,
    title: 'One-Tap Booking',
    description: 'Secure vetted stays, private transport, and master guides in a single, elegant checkout flow.',
    image: 'https://lh3.googleusercontent.com/aida-public/AB6AXuDOHkiHUHYhD5cVGGI0MBSYGTV3axLAbHChhbKc5Us-HozIcH2Wjh6bTPwqtqt_k0LkqbKYsdLSpP7FKx8lhXV4T-CL_ANwBh6tEnn1EYwqSm11wv3gTATegFUv3XUUQmlfDDxE27ADHe6OaNHOaD01R_mmTHQ2cKRwl0-xHleXWj1ONADITf1esI-PigO0bxpM_7p7QNa5fVE1-a7luem4tWlZhmNOKwyd2pYxh4SPhLjWZLHq3BGtVnbmYFKGCaZcF5WBalmFa6o',
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
  {
    name: 'Ella',
    tagline: 'Highlands',
    description: 'Mist-covered peaks and century-old tea rituals in the heart of the mountains.',
    image: 'https://lh3.googleusercontent.com/aida-public/AB6AXuCCYzz_BNEj079XBI2d_J07LDQ88GzTbnW6v33aTabD7YexY0Gnsy-58NJjbL-3br_jMcipgY24GoTEQMMCYW7sSsl2lwwuGfSu00bSND_fuQG2ZQZPpBxRNK96vg-L3paIShC6I9vshwASSD_Gx3Wu_AMG1Iw3RGtrp1Zu11moygl3wniV9flNkl1ChE-k0xlUVzQPixASAg96PIPWVYDQrEWqWqT7mIQpKGFKm7PlyMyE_Cc1KuI7HKDkSuFhbvggie24K36wOwI',
  },
  {
    name: 'Galle',
    tagline: 'Coastal',
    description: 'Colonial history meets modern boutique luxury along the turquoise Indian Ocean.',
    image: 'https://lh3.googleusercontent.com/aida-public/AB6AXuDkLFadMUnD9bj4A-2B91VglCqYn8rGFFiIG3-h9NlUDQRCuLm4gqCSddXYf-kivCFw1BKvxBVm14F7Mpkj94X26spb2ozkOHV8kVopjdCPuSpCgr2El_zIwnvRZ8DVlaiUXnatUWyDhlDKDLIz9nO8wGeQrjRAP-TQA1-ixZRWmvA-CF4VFJ0bzNQ-9c_isz23kFeW7U6OIHWBtcRtAd8rzLhZpANRcFSgNcg8Q7eb5KJe3zQ1sG8pv4Fo-DQj7FHbPZqlHbsdjmI',
  },
  {
    name: 'Sigiriya',
    tagline: 'Heritage',
    description: 'The 8th wonder of the ancient world. A royal citadel atop a monolithic rock.',
    image: 'https://lh3.googleusercontent.com/aida-public/AB6AXuCxlUODaq5mrqpX3XDDZTEuwi7miK3u9rAi_bkXOVUamSoQ6gsOY9-jQXbyXp8JZPWDUMkbTXgtu3fPPwotnuMK62eugehQUzYH1guiBc2v3wV0smSDdFFsVz9hXpXudjIhKBlIW2XOVp18p_A-6sfYeo8jwHHQERYRgctHjq5QHmXkJ20R6PCvdUqh6tryUbgaLrsj4X5Mj5UFkTuXhk2rSARzxJp8L7tMM0TzsuRWfI4upbq6Elf7Gyn_7VU9TDgSbmW_WDWC87I',
  },
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
    description: 'List your property and reach thousands of travelers visiting Sri Lanka. Manage bookings, rooms, and reviews all in one place.',
    cta: 'List Your Property',
    href: '/register?role=HOTEL_OWNER',
  },
  {
    icon: MapPin,
    title: 'Tour Guides',
    description: 'Share your local expertise with travelers from around the world. Set your own rates, manage your schedule, and grow your reputation.',
    cta: 'Become a Guide',
    href: '/register?role=TOUR_GUIDE',
  },
  {
    icon: Car,
    title: 'Vehicle Owners',
    description: 'Rent your vehicles to tourists and earn. From tuk-tuks to luxury cars, reach travelers looking for reliable transport.',
    cta: 'Register Your Vehicle',
    href: '/register?role=VEHICLE_OWNER',
  },
]

const jsonLd = {
  '@context': 'https://schema.org',
  '@type': 'WebApplication',
  name: 'TravelPlan',
  description: 'AI-powered travel planning platform for Sri Lanka tourism. Chat with AI, get personalized itineraries, and book hotels, tour guides, and vehicles.',
  applicationCategory: 'TravelApplication',
  operatingSystem: 'Web',
  offers: {
    '@type': 'Offer',
    price: '0',
    priceCurrency: 'USD',
    description: 'Free to plan, pay only for bookings',
  },
  aggregateRating: {
    '@type': 'AggregateRating',
    ratingValue: '4.8',
    ratingCount: '1000',
    bestRating: '5',
  },
  areaServed: {
    '@type': 'Country',
    name: 'Sri Lanka',
  },
}

export default function LandingPage() {
  return (
    <main className="min-h-screen bg-background font-body antialiased">
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
      />
      {/* Navigation */}
      <nav className="fixed top-0 w-full z-50 bg-white/90 backdrop-blur-xl border-b border-border/30">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <Link href="/" className="flex items-center gap-2">
            <span className="font-display text-xl font-extrabold text-foreground tracking-tight">
              Travel<span className="text-primary">Plan</span>
            </span>
          </Link>

          <div className="hidden md:flex items-center gap-10 font-display text-[13px] font-bold tracking-wide uppercase">
            <a href="#features" className="text-primary border-b-2 border-primary pb-1">
              Explore
            </a>
            <a href="#how-it-works" className="text-muted-foreground hover:text-primary transition-colors">
              AI Planner
            </a>
            <a href="#destinations" className="text-muted-foreground hover:text-primary transition-colors">
              Destinations
            </a>
            <a href="#providers" className="text-muted-foreground hover:text-primary transition-colors">
              Our Story
            </a>
          </div>

          <div className="flex items-center gap-4">
            <Link href="/register?role=HOTEL_OWNER" className="hidden lg:block text-muted-foreground font-display text-sm font-semibold hover:text-primary transition-colors">
              Join as Provider
            </Link>
            <Button asChild className="bg-foreground text-white hover:bg-foreground/90 font-display text-sm font-bold px-7 py-2.5 rounded-full">
              <Link href="/login">Sign In</Link>
            </Button>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="relative min-h-[95vh] flex items-center justify-center overflow-hidden pt-16">
        {/* Background image */}
        <div className="absolute inset-0 z-0">
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img
            className="w-full h-full object-cover"
            src="https://lh3.googleusercontent.com/aida-public/AB6AXuD18toubO5ESMjXL8Rw_8CDF43TZklFcuJDo912tzv-5cRyphcoE1lM_QOg0a6tbS2Aa4hf8BxlR_npx_eKLaUQHcDis3Rconqj_PjBrfWJ6qdfgvyqN5RX7zBdGtm-5PSIJUq8yQEZ-Uau7zGd69Hp6kviqoHLv7Jl2icQa6QdheDhRG3pnZXsWUY9YqTRcmgLvSyfNnPUOw92mRXK6ENnDLRY1y8BeyEvhGv2IkVCHJmra3V3tXdPeFstk5iQaIQoS4BLNNS_eIM"
            alt="Sri Lankan tea plantations at sunrise"
          />
          <div className="absolute inset-0 bg-black/20" />
          <div className="absolute inset-0 bg-gradient-to-t from-background via-transparent to-black/10" />
        </div>

        <div className="relative z-10 w-full max-w-5xl px-6 text-center">
          <div className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-white/10 backdrop-blur-md text-white text-sm font-medium mb-8 border border-white/20">
            <Sparkles className="h-4 w-4" />
            AI-Powered Travel Planning
          </div>

          <h1 className="font-display text-6xl md:text-[5.5rem] font-extrabold text-white tracking-tight leading-[1.05] mb-6 drop-shadow-sm">
            Sri Lanka,{' '}
            <br className="hidden sm:block" />
            <span className="text-primary-container-foreground">
              Curated for You.
            </span>
          </h1>

          <p className="text-lg sm:text-xl text-white/80 max-w-2xl mx-auto leading-relaxed mb-10">
            Plan your perfect adventure in minutes. Chat with our AI, get personalized
            itineraries with the best local hotels, guides, and transport.
          </p>

          {/* Search/Plan Bar */}
          <div className="max-w-3xl mx-auto glass-card p-2 md:p-3 rounded-2xl editorial-shadow flex items-center gap-2">
            <div className="flex-1 flex items-center px-5 gap-4">
              <Sparkles className="h-5 w-5 text-primary shrink-0" />
              <input
                className="w-full bg-transparent border-none focus:outline-none text-foreground text-lg placeholder:text-muted-foreground/60 font-body"
                placeholder="I want to see elephants and tea plantations in March..."
                type="text"
                readOnly
              />
            </div>
            <Button asChild className="bg-primary text-white px-8 py-6 rounded-xl font-display font-bold flex items-center gap-2 shadow-lg shadow-primary/20">
              <Link href="/register">
                Plan <ArrowRight className="h-4 w-4" />
              </Link>
            </Button>
          </div>

          {/* Stats */}
          <div className="mt-16 grid grid-cols-2 sm:grid-cols-4 gap-6 max-w-2xl mx-auto">
            {STATS.map(({ value, label, icon: Icon }) => (
              <div key={label} className="text-center">
                <div className="inline-flex items-center justify-center w-10 h-10 rounded-full bg-white/10 backdrop-blur-sm mb-2">
                  <Icon className="h-5 w-5 text-white" />
                </div>
                <div className="font-display text-2xl font-bold text-white">{value}</div>
                <div className="text-sm text-white/70">{label}</div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Features: The Power of AI Planning */}
      <section id="features" className="py-24 md:py-32 px-6 max-w-7xl mx-auto">
        <div className="text-center mb-16 md:mb-20">
          <p className="font-display text-sm font-extrabold text-primary tracking-[0.2em] uppercase mb-4">Sophisticated Technology</p>
          <h2 className="font-display text-4xl md:text-5xl font-extrabold text-foreground tracking-tight mb-6">
            The Power of AI Planning
          </h2>
          <p className="text-muted-foreground max-w-2xl mx-auto text-lg leading-relaxed">
            A seamless fusion of cutting-edge intelligence and deep local soul, designed for the modern explorer.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-12">
          {FEATURES.map(({ title, description, image }) => (
            <div key={title} className="group">
              <div className="w-full aspect-[4/3] rounded-2xl overflow-hidden mb-8 editorial-shadow">
                {/* eslint-disable-next-line @next/next/no-img-element */}
                <img
                  className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-700"
                  src={image}
                  alt={title}
                />
              </div>
              <h3 className="font-display text-2xl font-extrabold mb-4 tracking-tight">{title}</h3>
              <p className="text-muted-foreground leading-relaxed">{description}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Trending Destinations */}
      <section id="destinations" className="py-24 md:py-32 bg-surface-low">
        <div className="max-w-7xl mx-auto px-6 sm:px-8">
          <div className="flex flex-col md:flex-row justify-between items-end mb-16 gap-6">
            <div className="max-w-xl">
              <h2 className="font-display text-4xl md:text-5xl font-extrabold text-foreground tracking-tight mb-4">
                Trending Destinations
              </h2>
              <p className="text-muted-foreground text-lg">
                Verified by local specialists and curated by our AI for the optimal seasonal experience.
              </p>
            </div>
            <Link href="/register" className="text-primary font-bold text-sm tracking-widest uppercase flex items-center gap-3 group">
              View all destinations
              <ArrowRight className="h-4 w-4 group-hover:translate-x-1 transition-transform" />
            </Link>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 md:gap-10">
            {DESTINATIONS.map(({ name, tagline, description, image }, i) => (
              <Link
                key={name}
                href="/register"
                className={`group relative h-[500px] md:h-[600px] rounded-3xl overflow-hidden editorial-shadow ${i === 1 ? 'md:mt-16' : ''}`}
              >
                {/* eslint-disable-next-line @next/next/no-img-element */}
                <img
                  className="absolute inset-0 w-full h-full object-cover group-hover:scale-110 transition-transform duration-1000"
                  src={image}
                  alt={name}
                />
                <div className="absolute inset-0 bg-gradient-to-t from-black/90 via-black/20 to-transparent" />
                <div className="absolute bottom-0 left-0 p-8 md:p-10 w-full">
                  <span className="bg-white/10 backdrop-blur-md text-white text-[10px] font-bold uppercase tracking-[0.2em] px-4 py-1.5 rounded-full mb-5 inline-block border border-white/20">
                    {tagline}
                  </span>
                  <h3 className="font-display text-3xl md:text-4xl font-extrabold text-white mb-3 tracking-tight">{name}</h3>
                  <p className="text-white/70 mb-8 text-sm leading-relaxed">{description}</p>
                  <span className="block w-full bg-white text-foreground py-4 rounded-xl font-display font-extrabold text-sm text-center group-hover:bg-white/90 transition-colors">
                    Start Planning
                  </span>
                </div>
              </Link>
            ))}
          </div>
        </div>
      </section>

      {/* How It Works */}
      <section id="how-it-works" className="py-24 md:py-32 px-6 max-w-7xl mx-auto">
        <div className="text-center max-w-2xl mx-auto mb-16">
          <p className="font-display text-sm font-extrabold text-primary tracking-[0.2em] uppercase mb-4">How It Works</p>
          <h2 className="font-display text-4xl md:text-5xl font-extrabold text-foreground tracking-tight">
            Three steps to your dream trip
          </h2>
        </div>

        <div className="grid md:grid-cols-3 gap-8 lg:gap-12 relative">
          {/* Connecting line (desktop only) */}
          <div className="hidden md:block absolute top-16 left-[20%] right-[20%] h-0.5 bg-gradient-to-r from-primary/10 via-primary/20 to-primary/10" />

          {STEPS.map(({ step, title, description, icon: Icon }) => (
            <div key={step} className="relative text-center">
              <div className="relative inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-primary text-white mb-6 shadow-lg shadow-primary/25">
                <Icon className="h-7 w-7" />
                <span className="absolute -top-2 -right-2 w-7 h-7 rounded-full bg-secondary text-white text-xs font-bold flex items-center justify-center shadow-sm">
                  {step}
                </span>
              </div>
              <h3 className="font-display text-xl font-bold text-foreground mb-3">{title}</h3>
              <p className="text-muted-foreground leading-relaxed max-w-sm mx-auto">{description}</p>
            </div>
          ))}
        </div>
      </section>

      {/* For Providers */}
      <section id="providers" className="py-24 md:py-32 px-6 max-w-7xl mx-auto">
        <div className="bg-surface-low rounded-[2rem] md:rounded-[3rem] overflow-hidden flex flex-col lg:flex-row items-stretch">
          {/* Content */}
          <div className="flex-1 p-10 lg:p-20">
            <p className="font-display text-sm font-extrabold text-primary tracking-[0.2em] uppercase mb-6">Partner with Us</p>
            <h2 className="font-display text-4xl md:text-5xl font-extrabold mb-8 tracking-tight text-foreground">
              Empowering Sri Lanka&apos;s Finest Hosts
            </h2>
            <p className="text-lg mb-10 text-muted-foreground leading-relaxed">
              We bridge the gap between world-class local hospitality and global travelers. Showcase your boutique villa or expert guide service to the right audience.
            </p>

            <div className="space-y-6 mb-10">
              <div className="flex items-center gap-5">
                <div className="w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center text-primary">
                  <Zap className="h-5 w-5" />
                </div>
                <span className="text-lg font-bold tracking-tight">Zero-friction bookings</span>
              </div>
              <div className="flex items-center gap-5">
                <div className="w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center text-primary">
                  <Globe className="h-5 w-5" />
                </div>
                <span className="text-lg font-bold tracking-tight">Global visibility & marketing</span>
              </div>
            </div>

            <Button asChild className="bg-foreground text-white hover:bg-foreground/90 px-10 py-6 rounded-2xl font-display font-extrabold text-base">
              <Link href="/register?role=HOTEL_OWNER">
                Join as a Local Partner
              </Link>
            </Button>
          </div>

          {/* Villa image */}
          <div className="flex-1 w-full relative overflow-hidden hidden lg:block min-h-[600px]">
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img
              className="w-full h-full object-cover"
              src="https://lh3.googleusercontent.com/aida-public/AB6AXuA_BT4ijHCpyY8r6OFvQlDmA2jWws8DKAc4Q32xm9s0irSxP37nS-kBSuFYjrHdzMWQzyxOaq9u3jOwge2O57jndQNiloI9OLIlyO7pGcG5tw4wHr9Wb_eusM-w6p7GI4iHucCxmL4EZK2FHaoqIdNRk7Q7eHX-IH3TUrSPA24HIjrkR-8gFb7kWgGnzurYSfpkneAAO4oUnEiNLVPPYpSz7CT-7KfrTzlfesEEyURrxsBfC37i9jGoHehgTjQmpmlk8tWITdL9vJI"
              alt="Luxury Sri Lankan villa with tropical views"
            />
            <div className="absolute inset-0 bg-primary/5" />
          </div>
          {/* Mobile: Provider cards */}
          <div className="lg:hidden w-full p-8 space-y-4">
            {PROVIDER_TYPES.map(({ icon: Icon, title, description, cta, href }) => (
              <Link
                key={title}
                href={href}
                className="group block p-6 rounded-2xl bg-white editorial-shadow hover:translate-y-[-2px] transition-all duration-300"
              >
                <div className="flex items-start gap-4">
                  <div className="w-11 h-11 rounded-xl bg-primary/10 flex items-center justify-center shrink-0">
                    <Icon className="h-5 w-5 text-primary" />
                  </div>
                  <div className="flex-1">
                    <h3 className="font-display text-lg font-bold text-foreground mb-1">{title}</h3>
                    <p className="text-sm text-muted-foreground leading-relaxed mb-3">{description}</p>
                    <span className="text-primary font-bold text-sm flex items-center gap-2 group-hover:gap-3 transition-all">
                      {cta} <ArrowRight className="h-4 w-4" />
                    </span>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        </div>
      </section>

      {/* Testimonials */}
      <section className="py-24 md:py-32 bg-white">
        <div className="max-w-7xl mx-auto px-6 sm:px-8">
          <h2 className="font-display text-4xl md:text-5xl font-extrabold text-foreground tracking-tight text-center mb-20">
            Shared Stories
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-12 md:gap-16">
            {/* Traveler testimonial */}
            <div className="relative pt-8">
              <span className="text-primary/10 text-8xl font-serif absolute -top-6 -left-2 select-none">&ldquo;</span>
              <p className="text-xl sm:text-2xl font-light italic leading-relaxed text-foreground mb-10 relative z-10">
                &ldquo;The AI Concierge felt like talking to a friend who knew every corner of the island. We found a small pottery village in the hills that wasn&apos;t in a single guidebook.&rdquo;
              </p>
              <div className="flex items-center gap-5">
                {/* eslint-disable-next-line @next/next/no-img-element */}
                <img
                  className="w-14 h-14 rounded-full object-cover border-2 border-surface-low shadow-sm"
                  src="https://lh3.googleusercontent.com/aida-public/AB6AXuAZXPkj-ltoZHvocSidpA3Xdugfhc_y3ilwENe5N_Iqng2N1HRnsh45_kVMW2VsGK-OhTaS17BlKL-EnRf3_fhDVjrIKzoT2wiTplpq-6JsrQvjeK6xeOff11BXVedN_2wm8-euZJpJdWcSdUD_n8FRCwEHtdDFFX8aZirtcu3WhQCVFeOuSsX5clIpddSo3tEFWgWH6yB8nn_ZrKNqyzJAdUBP-_8OXV7qs298xs6KRjaBYTIxqFMGYpLq5NTFfXACeHCUSwpErpY"
                  alt="Elena Rodriguez"
                />
                <div>
                  <p className="font-extrabold text-foreground text-lg">Elena Rodriguez</p>
                  <p className="text-sm text-muted-foreground uppercase tracking-widest font-bold">Solo Traveler, Madrid</p>
                </div>
              </div>
            </div>
            {/* Provider testimonial */}
            <div className="relative pt-8">
              <span className="text-primary/10 text-8xl font-serif absolute -top-6 -left-2 select-none">&ldquo;</span>
              <p className="text-xl sm:text-2xl font-light italic leading-relaxed text-foreground mb-10 relative z-10">
                &ldquo;Managing my boutique villa was once a chaos of messages. Now, the bookings are automated, and guests arrive with a deep appreciation for our heritage.&rdquo;
              </p>
              <div className="flex items-center gap-5">
                {/* eslint-disable-next-line @next/next/no-img-element */}
                <img
                  className="w-14 h-14 rounded-full object-cover border-2 border-surface-low shadow-sm"
                  src="https://lh3.googleusercontent.com/aida-public/AB6AXuCN7xrHpf6h3O-9pP7Ewiqiy11YsbvqDrIkwejV0ynQqTHxJcrHuoLd0GSYKSNfsGyKfyhU_q5C81epfX0IZVrH91NkIBIHxn0mlCpTPPbeZ684UhiajVb0ltiSdMB4bjB5XIWTfsxIsBH9gyWDsUUTO5mXpAE3k0ieXuYmtWCpJOkhNKsBzSVAmczj3_qGxLam0WMorkZDDuhFfOmtVncZ6fJTYlLOIC8WBaMzIzf1SPnd-LFltg7rnkLWUt4b6Ccr4nly6JLEpWg"
                  alt="Asanka Perera"
                />
                <div>
                  <p className="font-extrabold text-foreground text-lg">Asanka Perera</p>
                  <p className="text-sm text-muted-foreground uppercase tracking-widest font-bold">Villa Owner, Mirissa</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-24 relative overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-br from-primary via-primary to-tertiary" />
        <div className="absolute top-0 right-0 w-96 h-96 bg-white/5 rounded-full blur-3xl" />
        <div className="absolute bottom-0 left-0 w-96 h-96 bg-white/5 rounded-full blur-3xl" />

        <div className="relative max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h2 className="font-display text-3xl sm:text-4xl lg:text-5xl font-extrabold text-white leading-tight tracking-tight">
            Ready to plan your
            <br />Sri Lanka adventure?
          </h2>
          <p className="mt-6 text-lg text-white/80 max-w-xl mx-auto">
            Join thousands of travelers who planned their perfect trip with our AI companion. It takes less than 5 minutes.
          </p>
          <div className="mt-10 flex flex-col sm:flex-row gap-4 justify-center">
            <Button asChild size="lg" className="bg-white text-primary hover:bg-white/90 text-base px-8 rounded-full h-12 shadow-lg font-display font-bold">
              <Link href="/register">
                Get Started Free
                <ArrowRight className="ml-2 h-5 w-5" />
              </Link>
            </Button>
            <Button asChild variant="outline" size="lg" className="border-white/30 text-white hover:bg-white/10 text-base px-8 rounded-full h-12 font-display">
              <Link href="/login">
                I have an account
              </Link>
            </Button>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-surface-low border-t border-border/30">
        <div className="py-16 md:py-20">
          <div className="max-w-7xl mx-auto px-6 sm:px-10 grid grid-cols-2 md:grid-cols-5 gap-12 md:gap-16">
            <div className="col-span-2">
              <div className="text-xl font-extrabold font-display tracking-tight mb-6">Travel<span className="text-primary">Plan</span></div>
              <p className="text-muted-foreground text-sm leading-relaxed max-w-xs mb-8">
                AI-powered travel planning for Sri Lanka. Discover hotels, tour guides, and transport — all personalized to your adventure.
              </p>
            </div>
            <div>
              <h4 className="font-display text-[11px] uppercase tracking-[0.2em] font-extrabold mb-6 text-foreground">Company</h4>
              <ul className="space-y-4">
                <li><a className="text-muted-foreground hover:text-primary transition-all text-xs font-bold uppercase tracking-wider" href="#providers">Our Story</a></li>
                <li><Link className="text-muted-foreground hover:text-primary transition-all text-xs font-bold uppercase tracking-wider" href="/register?role=HOTEL_OWNER">Partner Portal</Link></li>
              </ul>
            </div>
            <div>
              <h4 className="font-display text-[11px] uppercase tracking-[0.2em] font-extrabold mb-6 text-foreground">Experience</h4>
              <ul className="space-y-4">
                <li><a className="text-muted-foreground hover:text-primary transition-all text-xs font-bold uppercase tracking-wider" href="#destinations">Destinations</a></li>
                <li><Link className="text-muted-foreground hover:text-primary transition-all text-xs font-bold uppercase tracking-wider" href="/register">AI Concierge</Link></li>
              </ul>
            </div>
            <div>
              <h4 className="font-display text-[11px] uppercase tracking-[0.2em] font-extrabold mb-6 text-foreground">Legal</h4>
              <ul className="space-y-4">
                <li><a className="text-muted-foreground hover:text-primary transition-all text-xs font-bold uppercase tracking-wider" href="#">Privacy Policy</a></li>
                <li><a className="text-muted-foreground hover:text-primary transition-all text-xs font-bold uppercase tracking-wider" href="#">Terms of Use</a></li>
              </ul>
            </div>
          </div>
          <div className="max-w-7xl mx-auto px-6 sm:px-10 mt-16 pt-8 border-t border-border/30">
            <p className="text-muted-foreground text-[10px] uppercase tracking-[0.3em] font-bold">
              &copy; {new Date().getFullYear()} TravelPlan. AI-powered travel planning for Sri Lanka.
            </p>
          </div>
        </div>
      </footer>
    </main>
  )
}

'use client'

import { useState, useEffect, Suspense } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import Link from 'next/link'
import { createClient } from '@/lib/supabase/client'
import { registerTourist } from '@/lib/api/tourist'
import { registerGuide } from '@/lib/api/guide'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardFooter } from '@/components/ui/card'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Textarea } from '@/components/ui/textarea'
import { toast } from 'sonner'
import {
  Compass,
  Star,
  Users,
  Globe,
  Building2,
  MapPin,
  Car,
  ArrowLeft,
  ArrowRight,
} from 'lucide-react'
import { GUIDE_LANGUAGES, GUIDE_SPECIALIZATIONS } from '@/types/guide'

type RegistrationStep = 'select-role' | 'tourist' | 'hotel_owner' | 'tour_guide' | 'vehicle_owner'

const BUDGET_LEVELS = [
  { value: 'BUDGET', label: 'Budget', description: '$30–80 / day' },
  { value: 'MODERATE', label: 'Moderate', description: '$80–200 / day' },
  { value: 'LUXURY', label: 'Luxury', description: '$200+ / day' },
] as const

const NATIONALITIES = [
  'Sri Lankan', 'Indian', 'British', 'American', 'Australian', 'German',
  'French', 'Chinese', 'Japanese', 'Canadian', 'Other',
]

const ROLE_CARDS = [
  {
    key: 'tourist' as const,
    icon: Compass,
    title: 'Tourist',
    description: 'Plan and book your perfect Sri Lanka trip with AI assistance.',
    color: 'border-teal-200 hover:border-teal-400 hover:bg-teal-50/50',
    iconColor: 'bg-teal-50 text-teal-600',
  },
  {
    key: 'hotel_owner' as const,
    icon: Building2,
    title: 'Hotel Owner',
    description: 'List your property and manage bookings from travelers.',
    color: 'border-blue-200 hover:border-blue-400 hover:bg-blue-50/50',
    iconColor: 'bg-blue-50 text-blue-600',
  },
  {
    key: 'tour_guide' as const,
    icon: MapPin,
    title: 'Tour Guide',
    description: 'Offer your local expertise and connect with travelers.',
    color: 'border-emerald-200 hover:border-emerald-400 hover:bg-emerald-50/50',
    iconColor: 'bg-emerald-50 text-emerald-600',
  },
  {
    key: 'vehicle_owner' as const,
    icon: Car,
    title: 'Vehicle Owner',
    description: 'Rent your vehicles to tourists visiting Sri Lanka.',
    color: 'border-orange-200 hover:border-orange-400 hover:bg-orange-50/50',
    iconColor: 'bg-orange-50 text-orange-500',
  },
]

const BRAND_CONTENT: Record<RegistrationStep, { heading: string; subtitle: string }> = {
  'select-role': {
    heading: 'Join\nTravelPlan.',
    subtitle: 'Choose how you want to use TravelPlan — as a traveler or a service provider.',
  },
  tourist: {
    heading: 'Start your\nadventure today.',
    subtitle: 'Create an account and let our AI plan your perfect Sri Lanka trip — personalized to your interests and budget.',
  },
  hotel_owner: {
    heading: 'List your\nproperty.',
    subtitle: 'Reach thousands of travelers visiting Sri Lanka. Manage bookings, rooms, and reviews all in one place.',
  },
  tour_guide: {
    heading: 'Share your\nexpertise.',
    subtitle: 'Connect with travelers from around the world. Set your own rates and grow your reputation.',
  },
  vehicle_owner: {
    heading: 'Grow your\nbusiness.',
    subtitle: 'Rent your vehicles to tourists and earn. Manage bookings and availability effortlessly.',
  },
}

const ROLE_META: Record<string, string> = {
  hotel_owner: 'HOTEL_OWNER',
  tour_guide: 'TOUR_GUIDE',
  vehicle_owner: 'VEHICLE_OWNER',
}

function RegisterContent() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const [step, setStep] = useState<RegistrationStep>('select-role')
  const [loading, setLoading] = useState(false)

  // Tourist form state
  const [touristForm, setTouristForm] = useState({
    email: '', password: '', confirmPassword: '',
    firstName: '', lastName: '', phoneNumber: '',
    nationality: '', preferredBudget: 'MODERATE',
  })

  // Provider form state (shared for hotel_owner, vehicle_owner)
  const [providerForm, setProviderForm] = useState({
    email: '', password: '', confirmPassword: '',
    firstName: '', lastName: '', phoneNumber: '',
  })

  // Tour guide form state (extends provider)
  const [guideForm, setGuideForm] = useState({
    email: '', password: '', confirmPassword: '',
    firstName: '', lastName: '', phoneNumber: '',
    hourlyRate: '', dailyRate: '', bio: '',
    experienceYears: '',
    languages: [] as string[],
    specializations: [] as string[],
  })

  // Auto-select role from query param
  useEffect(() => {
    const roleParam = searchParams.get('role')
    if (roleParam) {
      const mapping: Record<string, RegistrationStep> = {
        TOURIST: 'tourist',
        HOTEL_OWNER: 'hotel_owner',
        TOUR_GUIDE: 'tour_guide',
        VEHICLE_OWNER: 'vehicle_owner',
      }
      if (mapping[roleParam]) {
        setStep(mapping[roleParam])
      }
    }
  }, [searchParams])

  function handleTouristChange(e: React.ChangeEvent<HTMLInputElement>) {
    setTouristForm(prev => ({ ...prev, [e.target.name]: e.target.value }))
  }

  function handleProviderChange(e: React.ChangeEvent<HTMLInputElement>) {
    setProviderForm(prev => ({ ...prev, [e.target.name]: e.target.value }))
  }

  function handleGuideChange(e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    setGuideForm(prev => ({ ...prev, [e.target.name]: e.target.value }))
  }

  function toggleGuideChip(field: 'languages' | 'specializations', value: string) {
    setGuideForm(prev => ({
      ...prev,
      [field]: prev[field].includes(value)
        ? prev[field].filter(v => v !== value)
        : [...prev[field], value],
    }))
  }

  // Tourist registration
  async function handleTouristSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (touristForm.password !== touristForm.confirmPassword) {
      toast.error('Passwords do not match')
      return
    }
    if (touristForm.password.length < 6) {
      toast.error('Password must be at least 6 characters')
      return
    }

    setLoading(true)
    try {
      const supabase = createClient()
      const { data: authData, error: authError } = await supabase.auth.signUp({
        email: touristForm.email,
        password: touristForm.password,
        options: {
          data: {
            first_name: touristForm.firstName,
            last_name: touristForm.lastName,
            role: 'TOURIST',
          },
        },
      })

      if (authError) { toast.error(authError.message); return }
      if (!authData.user) { toast.error('Registration failed. Please try again.'); return }

      await registerTourist({
        userId: authData.user.id,
        email: touristForm.email,
        firstName: touristForm.firstName,
        lastName: touristForm.lastName,
        phoneNumber: touristForm.phoneNumber || undefined,
        nationality: touristForm.nationality || undefined,
        preferredBudget: touristForm.preferredBudget,
      })

      toast.success('Account created successfully!')
      router.push('/chat')
      router.refresh()
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  // Provider registration (Hotel Owner / Vehicle Owner)
  async function handleProviderSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (providerForm.password !== providerForm.confirmPassword) {
      toast.error('Passwords do not match')
      return
    }
    if (providerForm.password.length < 6) {
      toast.error('Password must be at least 6 characters')
      return
    }

    const role = ROLE_META[step]
    if (!role) return

    setLoading(true)
    try {
      const supabase = createClient()
      const { error: authError } = await supabase.auth.signUp({
        email: providerForm.email,
        password: providerForm.password,
        options: {
          data: {
            first_name: providerForm.firstName,
            last_name: providerForm.lastName,
            role,
          },
        },
      })

      if (authError) { toast.error(authError.message); return }

      toast.success('Account created successfully!')
      router.push('/provider/dashboard')
      router.refresh()
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  // Tour guide registration
  async function handleGuideSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (guideForm.password !== guideForm.confirmPassword) {
      toast.error('Passwords do not match')
      return
    }
    if (guideForm.password.length < 6) {
      toast.error('Password must be at least 6 characters')
      return
    }
    if (!guideForm.hourlyRate || !guideForm.dailyRate) {
      toast.error('Hourly rate and daily rate are required')
      return
    }

    setLoading(true)
    try {
      const supabase = createClient()
      const { data: authData, error: authError } = await supabase.auth.signUp({
        email: guideForm.email,
        password: guideForm.password,
        options: {
          data: {
            first_name: guideForm.firstName,
            last_name: guideForm.lastName,
            role: 'TOUR_GUIDE',
          },
        },
      })

      if (authError) { toast.error(authError.message); return }
      if (!authData.user) { toast.error('Registration failed. Please try again.'); return }

      await registerGuide({
        firstName: guideForm.firstName,
        lastName: guideForm.lastName,
        email: guideForm.email,
        phoneNumber: guideForm.phoneNumber || undefined,
        hourlyRate: parseFloat(guideForm.hourlyRate),
        dailyRate: parseFloat(guideForm.dailyRate),
        bio: guideForm.bio || undefined,
        experienceYears: guideForm.experienceYears ? parseInt(guideForm.experienceYears) : undefined,
        languages: guideForm.languages.length > 0 ? guideForm.languages : undefined,
        specializations: guideForm.specializations.length > 0 ? guideForm.specializations : undefined,
      })

      toast.success('Account created successfully!')
      router.push('/provider/dashboard')
      router.refresh()
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  const brand = BRAND_CONTENT[step]

  return (
    <main className="min-h-screen flex">
      {/* Brand Panel */}
      <div className="hidden lg:flex lg:w-[45%] relative overflow-hidden bg-gradient-to-br from-primary via-teal-600 to-emerald-600">
        <div className="absolute top-0 right-0 w-96 h-96 bg-white/5 rounded-full blur-3xl" />
        <div className="absolute bottom-0 left-0 w-80 h-80 bg-white/5 rounded-full blur-3xl" />

        <div className="relative flex flex-col justify-between p-12 w-full">
          <Link href="/" className="flex items-center gap-2">
            <div className="h-9 w-9 rounded-lg bg-white/20 flex items-center justify-center backdrop-blur-sm">
              <Compass className="h-5 w-5 text-white" />
            </div>
            <span className="font-display text-xl font-bold text-white">TravelPlan</span>
          </Link>

          <div className="space-y-8">
            <h2 className="font-display text-4xl font-bold text-white leading-tight whitespace-pre-line">
              {brand.heading}
            </h2>
            <p className="text-white/70 text-lg max-w-md">
              {brand.subtitle}
            </p>

            <div className="grid grid-cols-3 gap-4 pt-4">
              {[
                { icon: Star, value: '4.8', label: 'Rating' },
                { icon: Users, value: '1K+', label: 'Travelers' },
                { icon: Globe, value: '25+', label: 'Destinations' },
              ].map(({ icon: Icon, value, label }) => (
                <div key={label} className="text-center">
                  <div className="inline-flex items-center justify-center w-10 h-10 rounded-xl bg-white/10 mb-2">
                    <Icon className="h-5 w-5 text-white" />
                  </div>
                  <div className="font-display text-xl font-bold text-white">{value}</div>
                  <div className="text-xs text-white/60">{label}</div>
                </div>
              ))}
            </div>
          </div>

          <p className="text-white/40 text-sm">
            &copy; {new Date().getFullYear()} TravelPlan
          </p>
        </div>
      </div>

      {/* Form Panel */}
      <div className="flex-1 flex items-center justify-center p-6 sm:p-8 bg-gradient-to-b from-teal-50/30 to-white lg:from-white lg:to-white overflow-y-auto">
        <div className="w-full max-w-lg">
          {/* Mobile logo */}
          <div className="lg:hidden flex items-center justify-center gap-2 mb-6">
            <div className="h-9 w-9 rounded-lg bg-primary flex items-center justify-center">
              <Compass className="h-5 w-5 text-white" />
            </div>
            <span className="font-display text-xl font-bold">
              Travel<span className="text-primary">Plan</span>
            </span>
          </div>

          {/* === ROLE SELECTION === */}
          {step === 'select-role' && (
            <div>
              <div className="mb-8">
                <h1 className="font-display text-2xl sm:text-3xl font-bold text-foreground">Create Account</h1>
                <p className="text-muted-foreground mt-2">How would you like to use TravelPlan?</p>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                {ROLE_CARDS.map(({ key, icon: Icon, title, description, color, iconColor }) => (
                  <button
                    key={key}
                    onClick={() => setStep(key)}
                    className={`text-left p-5 rounded-xl border-2 transition-all duration-200 ${color}`}
                  >
                    <div className={`inline-flex items-center justify-center w-11 h-11 rounded-lg ${iconColor} mb-3`}>
                      <Icon className="h-5 w-5" />
                    </div>
                    <h3 className="font-display text-base font-semibold text-foreground mb-1">{title}</h3>
                    <p className="text-sm text-muted-foreground leading-relaxed">{description}</p>
                  </button>
                ))}
              </div>

              <p className="text-center text-sm text-muted-foreground mt-6">
                Already have an account?{' '}
                <Link href="/login" className="text-primary font-semibold hover:underline">
                  Sign In
                </Link>
              </p>
            </div>
          )}

          {/* === TOURIST FORM === */}
          {step === 'tourist' && (
            <div>
              <div className="mb-6">
                <button onClick={() => setStep('select-role')} className="flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground mb-4 transition-colors">
                  <ArrowLeft className="h-4 w-4" /> Back
                </button>
                <h1 className="font-display text-2xl sm:text-3xl font-bold text-foreground">Tourist Account</h1>
                <p className="text-muted-foreground mt-2">Start planning your Sri Lanka adventure</p>
              </div>

              <Card className="border-0 shadow-none lg:border lg:shadow-sm">
                <form onSubmit={handleTouristSubmit}>
                  <CardContent className="space-y-5 px-0 lg:px-6 pt-0 lg:pt-6">
                    <div className="grid grid-cols-2 gap-3">
                      <div className="space-y-2">
                        <Label htmlFor="firstName">First Name</Label>
                        <Input id="firstName" name="firstName" value={touristForm.firstName} onChange={handleTouristChange} required className="h-11" placeholder="John" />
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="lastName">Last Name</Label>
                        <Input id="lastName" name="lastName" value={touristForm.lastName} onChange={handleTouristChange} required className="h-11" placeholder="Doe" />
                      </div>
                    </div>

                    <div className="space-y-2">
                      <Label htmlFor="email">Email address</Label>
                      <Input id="email" name="email" type="email" value={touristForm.email} onChange={handleTouristChange} required className="h-11" placeholder="you@example.com" />
                    </div>

                    <div className="grid grid-cols-2 gap-3">
                      <div className="space-y-2">
                        <Label htmlFor="password">Password</Label>
                        <Input id="password" name="password" type="password" value={touristForm.password} onChange={handleTouristChange} required minLength={6} className="h-11" placeholder="Min 6 chars" />
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="confirmPassword">Confirm</Label>
                        <Input id="confirmPassword" name="confirmPassword" type="password" value={touristForm.confirmPassword} onChange={handleTouristChange} required minLength={6} className="h-11" placeholder="Re-enter" />
                      </div>
                    </div>

                    <div className="relative">
                      <div className="absolute inset-0 flex items-center"><span className="w-full border-t" /></div>
                      <div className="relative flex justify-center text-xs uppercase">
                        <span className="bg-white lg:bg-card px-2 text-muted-foreground">Optional details</span>
                      </div>
                    </div>

                    <div className="space-y-2">
                      <Label htmlFor="phoneNumber">Phone Number</Label>
                      <Input id="phoneNumber" name="phoneNumber" type="tel" value={touristForm.phoneNumber} onChange={handleTouristChange} className="h-11" placeholder="+94 77 123 4567" />
                    </div>

                    <div className="grid grid-cols-2 gap-3">
                      <div className="space-y-2">
                        <Label>Nationality</Label>
                        <Select value={touristForm.nationality} onValueChange={(v) => setTouristForm(prev => ({ ...prev, nationality: v }))}>
                          <SelectTrigger className="h-11"><SelectValue placeholder="Select..." /></SelectTrigger>
                          <SelectContent>
                            {NATIONALITIES.map(n => (
                              <SelectItem key={n} value={n}>{n}</SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      </div>
                      <div className="space-y-2">
                        <Label>Budget Level</Label>
                        <Select value={touristForm.preferredBudget} onValueChange={(v) => setTouristForm(prev => ({ ...prev, preferredBudget: v }))}>
                          <SelectTrigger className="h-11"><SelectValue /></SelectTrigger>
                          <SelectContent>
                            {BUDGET_LEVELS.map(b => (
                              <SelectItem key={b.value} value={b.value}>
                                <span>{b.label}</span>
                                <span className="text-muted-foreground ml-1 text-xs">{b.description}</span>
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      </div>
                    </div>
                  </CardContent>

                  <CardFooter className="flex flex-col gap-4 px-0 lg:px-6 pb-0 lg:pb-6">
                    <Button type="submit" className="w-full h-11 rounded-lg text-base" disabled={loading}>
                      {loading ? 'Creating account...' : 'Create Account'}
                    </Button>
                    <p className="text-center text-sm text-muted-foreground">
                      Already have an account?{' '}
                      <Link href="/login" className="text-primary font-semibold hover:underline">Sign In</Link>
                    </p>
                  </CardFooter>
                </form>
              </Card>
            </div>
          )}

          {/* === HOTEL OWNER / VEHICLE OWNER FORM === */}
          {(step === 'hotel_owner' || step === 'vehicle_owner') && (
            <div>
              <div className="mb-6">
                <button onClick={() => setStep('select-role')} className="flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground mb-4 transition-colors">
                  <ArrowLeft className="h-4 w-4" /> Back
                </button>
                <h1 className="font-display text-2xl sm:text-3xl font-bold text-foreground">
                  {step === 'hotel_owner' ? 'Hotel Owner Account' : 'Vehicle Owner Account'}
                </h1>
                <p className="text-muted-foreground mt-2">
                  {step === 'hotel_owner'
                    ? 'Register to list your property on TravelPlan'
                    : 'Register to rent your vehicles to travelers'}
                </p>
              </div>

              <Card className="border-0 shadow-none lg:border lg:shadow-sm">
                <form onSubmit={handleProviderSubmit}>
                  <CardContent className="space-y-5 px-0 lg:px-6 pt-0 lg:pt-6">
                    <div className="grid grid-cols-2 gap-3">
                      <div className="space-y-2">
                        <Label htmlFor="pFirstName">First Name</Label>
                        <Input id="pFirstName" name="firstName" value={providerForm.firstName} onChange={handleProviderChange} required className="h-11" placeholder="John" />
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="pLastName">Last Name</Label>
                        <Input id="pLastName" name="lastName" value={providerForm.lastName} onChange={handleProviderChange} required className="h-11" placeholder="Doe" />
                      </div>
                    </div>

                    <div className="space-y-2">
                      <Label htmlFor="pEmail">Email address</Label>
                      <Input id="pEmail" name="email" type="email" value={providerForm.email} onChange={handleProviderChange} required className="h-11" placeholder="you@example.com" />
                    </div>

                    <div className="grid grid-cols-2 gap-3">
                      <div className="space-y-2">
                        <Label htmlFor="pPassword">Password</Label>
                        <Input id="pPassword" name="password" type="password" value={providerForm.password} onChange={handleProviderChange} required minLength={6} className="h-11" placeholder="Min 6 chars" />
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="pConfirmPassword">Confirm</Label>
                        <Input id="pConfirmPassword" name="confirmPassword" type="password" value={providerForm.confirmPassword} onChange={handleProviderChange} required minLength={6} className="h-11" placeholder="Re-enter" />
                      </div>
                    </div>

                    <div className="space-y-2">
                      <Label htmlFor="pPhoneNumber">Phone Number <span className="text-muted-foreground text-xs">(optional)</span></Label>
                      <Input id="pPhoneNumber" name="phoneNumber" type="tel" value={providerForm.phoneNumber} onChange={handleProviderChange} className="h-11" placeholder="+94 77 123 4567" />
                    </div>
                  </CardContent>

                  <CardFooter className="flex flex-col gap-4 px-0 lg:px-6 pb-0 lg:pb-6">
                    <Button type="submit" className="w-full h-11 rounded-lg text-base" disabled={loading}>
                      {loading ? 'Creating account...' : 'Create Account'}
                    </Button>
                    <p className="text-center text-sm text-muted-foreground">
                      Already have an account?{' '}
                      <Link href="/login" className="text-primary font-semibold hover:underline">Sign In</Link>
                    </p>
                  </CardFooter>
                </form>
              </Card>
            </div>
          )}

          {/* === TOUR GUIDE FORM === */}
          {step === 'tour_guide' && (
            <div>
              <div className="mb-6">
                <button onClick={() => setStep('select-role')} className="flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground mb-4 transition-colors">
                  <ArrowLeft className="h-4 w-4" /> Back
                </button>
                <h1 className="font-display text-2xl sm:text-3xl font-bold text-foreground">Tour Guide Account</h1>
                <p className="text-muted-foreground mt-2">Register to offer your expertise to travelers</p>
              </div>

              <Card className="border-0 shadow-none lg:border lg:shadow-sm">
                <form onSubmit={handleGuideSubmit}>
                  <CardContent className="space-y-5 px-0 lg:px-6 pt-0 lg:pt-6">
                    <div className="grid grid-cols-2 gap-3">
                      <div className="space-y-2">
                        <Label htmlFor="gFirstName">First Name</Label>
                        <Input id="gFirstName" name="firstName" value={guideForm.firstName} onChange={handleGuideChange} required className="h-11" placeholder="John" />
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="gLastName">Last Name</Label>
                        <Input id="gLastName" name="lastName" value={guideForm.lastName} onChange={handleGuideChange} required className="h-11" placeholder="Doe" />
                      </div>
                    </div>

                    <div className="space-y-2">
                      <Label htmlFor="gEmail">Email address</Label>
                      <Input id="gEmail" name="email" type="email" value={guideForm.email} onChange={handleGuideChange} required className="h-11" placeholder="you@example.com" />
                    </div>

                    <div className="grid grid-cols-2 gap-3">
                      <div className="space-y-2">
                        <Label htmlFor="gPassword">Password</Label>
                        <Input id="gPassword" name="password" type="password" value={guideForm.password} onChange={handleGuideChange} required minLength={6} className="h-11" placeholder="Min 6 chars" />
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="gConfirmPassword">Confirm</Label>
                        <Input id="gConfirmPassword" name="confirmPassword" type="password" value={guideForm.confirmPassword} onChange={handleGuideChange} required minLength={6} className="h-11" placeholder="Re-enter" />
                      </div>
                    </div>

                    <div className="space-y-2">
                      <Label htmlFor="gPhoneNumber">Phone Number <span className="text-muted-foreground text-xs">(optional)</span></Label>
                      <Input id="gPhoneNumber" name="phoneNumber" type="tel" value={guideForm.phoneNumber} onChange={handleGuideChange} className="h-11" placeholder="+94 77 123 4567" />
                    </div>

                    {/* Guide Profile Section */}
                    <div className="relative">
                      <div className="absolute inset-0 flex items-center"><span className="w-full border-t" /></div>
                      <div className="relative flex justify-center text-xs uppercase">
                        <span className="bg-white lg:bg-card px-2 text-muted-foreground">Guide Profile</span>
                      </div>
                    </div>

                    <div className="grid grid-cols-2 gap-3">
                      <div className="space-y-2">
                        <Label htmlFor="hourlyRate">Hourly Rate (USD)</Label>
                        <Input id="hourlyRate" name="hourlyRate" type="number" min="1" step="0.01" value={guideForm.hourlyRate} onChange={handleGuideChange} required className="h-11" placeholder="25.00" />
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="dailyRate">Daily Rate (USD)</Label>
                        <Input id="dailyRate" name="dailyRate" type="number" min="1" step="0.01" value={guideForm.dailyRate} onChange={handleGuideChange} required className="h-11" placeholder="150.00" />
                      </div>
                    </div>

                    <div className="space-y-2">
                      <Label htmlFor="experienceYears">Years of Experience <span className="text-muted-foreground text-xs">(optional)</span></Label>
                      <Input id="experienceYears" name="experienceYears" type="number" min="0" max="50" value={guideForm.experienceYears} onChange={handleGuideChange} className="h-11" placeholder="5" />
                    </div>

                    <div className="space-y-2">
                      <Label htmlFor="bio">Bio <span className="text-muted-foreground text-xs">(optional)</span></Label>
                      <Textarea id="bio" name="bio" value={guideForm.bio} onChange={handleGuideChange} rows={3} placeholder="Tell travelers about yourself and what makes your tours special..." className="resize-none" />
                    </div>

                    <div className="space-y-2">
                      <Label>Languages <span className="text-muted-foreground text-xs">(select all that apply)</span></Label>
                      <div className="flex flex-wrap gap-2">
                        {GUIDE_LANGUAGES.map(lang => (
                          <button
                            key={lang}
                            type="button"
                            onClick={() => toggleGuideChip('languages', lang)}
                            className={`px-3 py-1.5 rounded-full text-sm font-medium border transition-colors ${
                              guideForm.languages.includes(lang)
                                ? 'bg-primary text-white border-primary'
                                : 'bg-white text-muted-foreground border-border hover:border-primary/50'
                            }`}
                          >
                            {lang}
                          </button>
                        ))}
                      </div>
                    </div>

                    <div className="space-y-2">
                      <Label>Specializations <span className="text-muted-foreground text-xs">(select all that apply)</span></Label>
                      <div className="flex flex-wrap gap-2">
                        {GUIDE_SPECIALIZATIONS.map(spec => (
                          <button
                            key={spec}
                            type="button"
                            onClick={() => toggleGuideChip('specializations', spec)}
                            className={`px-3 py-1.5 rounded-full text-sm font-medium border transition-colors capitalize ${
                              guideForm.specializations.includes(spec)
                                ? 'bg-primary text-white border-primary'
                                : 'bg-white text-muted-foreground border-border hover:border-primary/50'
                            }`}
                          >
                            {spec.replace('_', ' ')}
                          </button>
                        ))}
                      </div>
                    </div>
                  </CardContent>

                  <CardFooter className="flex flex-col gap-4 px-0 lg:px-6 pb-0 lg:pb-6">
                    <Button type="submit" className="w-full h-11 rounded-lg text-base" disabled={loading}>
                      {loading ? 'Creating account...' : 'Create Account'}
                    </Button>
                    <p className="text-center text-sm text-muted-foreground">
                      Already have an account?{' '}
                      <Link href="/login" className="text-primary font-semibold hover:underline">Sign In</Link>
                    </p>
                  </CardFooter>
                </form>
              </Card>
            </div>
          )}
        </div>
      </div>
    </main>
  )
}

export default function RegisterPage() {
  return (
    <Suspense>
      <RegisterContent />
    </Suspense>
  )
}

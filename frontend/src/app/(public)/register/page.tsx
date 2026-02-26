'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { createClient } from '@/lib/supabase/client'
import { registerTourist } from '@/lib/api/tourist'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardFooter } from '@/components/ui/card'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { toast } from 'sonner'
import { Compass, Star, Users, Globe } from 'lucide-react'

const BUDGET_LEVELS = [
  { value: 'BUDGET', label: 'Budget', description: '$30–80 / day' },
  { value: 'MODERATE', label: 'Moderate', description: '$80–200 / day' },
  { value: 'LUXURY', label: 'Luxury', description: '$200+ / day' },
] as const

const NATIONALITIES = [
  'Sri Lankan', 'Indian', 'British', 'American', 'Australian', 'German',
  'French', 'Chinese', 'Japanese', 'Canadian', 'Other',
]

export default function RegisterPage() {
  const router = useRouter()
  const [loading, setLoading] = useState(false)
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    confirmPassword: '',
    firstName: '',
    lastName: '',
    phoneNumber: '',
    nationality: '',
    preferredBudget: 'MODERATE',
  })

  function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
    setFormData(prev => ({ ...prev, [e.target.name]: e.target.value }))
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()

    if (formData.password !== formData.confirmPassword) {
      toast.error('Passwords do not match')
      return
    }
    if (formData.password.length < 6) {
      toast.error('Password must be at least 6 characters')
      return
    }

    setLoading(true)

    try {
      const supabase = createClient()
      const { data: authData, error: authError } = await supabase.auth.signUp({
        email: formData.email,
        password: formData.password,
        options: {
          data: {
            first_name: formData.firstName,
            last_name: formData.lastName,
            role: 'TOURIST',
          },
        },
      })

      if (authError) {
        toast.error(authError.message)
        return
      }
      if (!authData.user) {
        toast.error('Registration failed. Please try again.')
        return
      }

      await registerTourist({
        userId: authData.user.id,
        email: formData.email,
        firstName: formData.firstName,
        lastName: formData.lastName,
        phoneNumber: formData.phoneNumber || undefined,
        nationality: formData.nationality || undefined,
        preferredBudget: formData.preferredBudget,
      })

      toast.success('Account created successfully!')
      router.push('/profile')
      router.refresh()
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

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
            <h2 className="font-display text-4xl font-bold text-white leading-tight">
              Start your
              <br />adventure today.
            </h2>
            <p className="text-white/70 text-lg max-w-md">
              Create an account and let our AI plan your perfect Sri Lanka trip — personalized to your interests and budget.
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

          <div className="mb-6">
            <h1 className="font-display text-2xl sm:text-3xl font-bold text-foreground">Create Account</h1>
            <p className="text-muted-foreground mt-2">Start planning your Sri Lanka adventure</p>
          </div>

          <Card className="border-0 shadow-none lg:border lg:shadow-sm">
            <form onSubmit={handleSubmit}>
              <CardContent className="space-y-5 px-0 lg:px-6 pt-0 lg:pt-6">
                {/* Name fields */}
                <div className="grid grid-cols-2 gap-3">
                  <div className="space-y-2">
                    <Label htmlFor="firstName">First Name</Label>
                    <Input id="firstName" name="firstName" value={formData.firstName} onChange={handleChange} required className="h-11" placeholder="John" />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="lastName">Last Name</Label>
                    <Input id="lastName" name="lastName" value={formData.lastName} onChange={handleChange} required className="h-11" placeholder="Doe" />
                  </div>
                </div>

                {/* Email */}
                <div className="space-y-2">
                  <Label htmlFor="email">Email address</Label>
                  <Input id="email" name="email" type="email" value={formData.email} onChange={handleChange} required className="h-11" placeholder="you@example.com" />
                </div>

                {/* Password fields */}
                <div className="grid grid-cols-2 gap-3">
                  <div className="space-y-2">
                    <Label htmlFor="password">Password</Label>
                    <Input id="password" name="password" type="password" value={formData.password} onChange={handleChange} required minLength={6} className="h-11" placeholder="Min 6 chars" />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="confirmPassword">Confirm</Label>
                    <Input id="confirmPassword" name="confirmPassword" type="password" value={formData.confirmPassword} onChange={handleChange} required minLength={6} className="h-11" placeholder="Re-enter" />
                  </div>
                </div>

                {/* Divider */}
                <div className="relative">
                  <div className="absolute inset-0 flex items-center"><span className="w-full border-t" /></div>
                  <div className="relative flex justify-center text-xs uppercase">
                    <span className="bg-white lg:bg-card px-2 text-muted-foreground">Optional details</span>
                  </div>
                </div>

                {/* Phone */}
                <div className="space-y-2">
                  <Label htmlFor="phoneNumber">Phone Number</Label>
                  <Input id="phoneNumber" name="phoneNumber" type="tel" value={formData.phoneNumber} onChange={handleChange} className="h-11" placeholder="+94 77 123 4567" />
                </div>

                {/* Nationality & Budget */}
                <div className="grid grid-cols-2 gap-3">
                  <div className="space-y-2">
                    <Label>Nationality</Label>
                    <Select value={formData.nationality} onValueChange={(v) => setFormData(prev => ({ ...prev, nationality: v }))}>
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
                    <Select value={formData.preferredBudget} onValueChange={(v) => setFormData(prev => ({ ...prev, preferredBudget: v }))}>
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
                  <Link href="/login" className="text-primary font-semibold hover:underline">
                    Sign In
                  </Link>
                </p>
              </CardFooter>
            </form>
          </Card>
        </div>
      </div>
    </main>
  )
}

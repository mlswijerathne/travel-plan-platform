'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { createClient } from '@/lib/supabase/client'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardFooter } from '@/components/ui/card'
import { toast } from 'sonner'
import { Compass, Map, MessageSquare, Shield } from 'lucide-react'

export default function LoginPage() {
  const router = useRouter()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setLoading(true)

    try {
      const supabase = createClient()
      const { error } = await supabase.auth.signInWithPassword({ email, password })

      if (error) {
        toast.error(error.message)
        return
      }

      toast.success('Welcome back!')

      // Role-based redirect
      const { data: { session } } = await supabase.auth.getSession()
      let redirectTo = '/profile'
      if (session?.access_token) {
        const payload = JSON.parse(atob(session.access_token.split('.')[1]))
        const role = payload.user_metadata?.role || payload.app_metadata?.role || 'TOURIST'
        const providerRoles = ['HOTEL_OWNER', 'TOUR_GUIDE', 'VEHICLE_OWNER']
        if (providerRoles.includes(role)) redirectTo = '/provider/dashboard'
        else if (role === 'ADMIN') redirectTo = '/admin'
      }

      router.push(redirectTo)
      router.refresh()
    } catch {
      toast.error('An unexpected error occurred')
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="min-h-screen flex">
      {/* Brand Panel - hidden on mobile */}
      <div className="hidden lg:flex lg:w-1/2 relative overflow-hidden bg-gradient-to-br from-primary via-teal-600 to-emerald-600">
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
              Welcome back,
              <br />explorer.
            </h2>
            <p className="text-white/70 text-lg max-w-md">
              Your next Sri Lanka adventure is just a conversation away. Sign in to continue planning.
            </p>

            <div className="space-y-4">
              {[
                { icon: MessageSquare, text: 'AI-powered trip planning' },
                { icon: Map, text: 'Personalized itineraries' },
                { icon: Shield, text: 'Verified local providers' },
              ].map(({ icon: Icon, text }) => (
                <div key={text} className="flex items-center gap-3 text-white/80">
                  <div className="flex items-center justify-center w-8 h-8 rounded-lg bg-white/10">
                    <Icon className="h-4 w-4" />
                  </div>
                  <span className="text-sm font-medium">{text}</span>
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
      <div className="flex-1 flex items-center justify-center p-6 sm:p-8 bg-gradient-to-b from-teal-50/30 to-white lg:from-white lg:to-white">
        <div className="w-full max-w-md">
          {/* Mobile logo */}
          <div className="lg:hidden flex items-center justify-center gap-2 mb-8">
            <div className="h-9 w-9 rounded-lg bg-primary flex items-center justify-center">
              <Compass className="h-5 w-5 text-white" />
            </div>
            <span className="font-display text-xl font-bold">
              Travel<span className="text-primary">Plan</span>
            </span>
          </div>

          <div className="mb-8">
            <h1 className="font-display text-2xl sm:text-3xl font-bold text-foreground">Welcome Back</h1>
            <p className="text-muted-foreground mt-2">Sign in to plan your next adventure</p>
          </div>

          <Card className="border-0 shadow-none lg:border lg:shadow-sm">
            <form onSubmit={handleSubmit}>
              <CardContent className="space-y-5 px-0 lg:px-6 pt-0 lg:pt-6">
                <div className="space-y-2">
                  <Label htmlFor="email">Email address</Label>
                  <Input
                    id="email"
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                    placeholder="you@example.com"
                    className="h-11"
                  />
                </div>

                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <Label htmlFor="password">Password</Label>
                    <button type="button" className="text-xs text-primary hover:underline">
                      Forgot password?
                    </button>
                  </div>
                  <Input
                    id="password"
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    placeholder="Enter your password"
                    className="h-11"
                  />
                </div>
              </CardContent>

              <CardFooter className="flex flex-col gap-4 px-0 lg:px-6 pb-0 lg:pb-6">
                <Button type="submit" className="w-full h-11 rounded-lg text-base" disabled={loading}>
                  {loading ? 'Signing in...' : 'Sign In'}
                </Button>
                <p className="text-center text-sm text-muted-foreground">
                  Don&apos;t have an account?{' '}
                  <Link href="/register" className="text-primary font-semibold hover:underline">
                    Create one
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

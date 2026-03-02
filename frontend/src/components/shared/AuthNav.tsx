'use client'

import Link from 'next/link'
import { usePathname, useRouter } from 'next/navigation'
import { createClient } from '@/lib/supabase/client'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import {
  Sheet,
  SheetContent,
  SheetTrigger,
} from '@/components/ui/sheet'
import { Avatar, AvatarFallback } from '@/components/ui/avatar'
import { cn } from '@/lib/utils'
import {
  Compass,
  MessageSquare,
  User,
  Calendar,
  Wallet,
  Menu,
  LogOut,
  Hotel,
  MapPin,
  Star,
  LayoutDashboard,
} from 'lucide-react'
import { useState } from 'react'
import { useUserRole } from '@/hooks/use-user-role'

const TOURIST_NAV = [
  { href: '/chat', label: 'Plan Trip', icon: MessageSquare },
  { href: '/hotels', label: 'Hotels', icon: Hotel },
  { href: '/guides', label: 'Guides', icon: MapPin },
  { href: '/bookings', label: 'Bookings', icon: Calendar },
  { href: '/reviews', label: 'Reviews', icon: Star },
  { href: '/profile', label: 'Profile', icon: User },
]

const PROVIDER_NAV = [
  { href: '/provider/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { href: '/provider/bookings', label: 'Bookings', icon: Calendar },
  { href: '/provider/reviews', label: 'Reviews', icon: Star },
  { href: '/profile', label: 'Profile', icon: User },
]

const HOTEL_OWNER_NAV = [
  { href: '/provider/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { href: '/provider/hotels', label: 'My Hotels', icon: Hotel },
  { href: '/provider/bookings', label: 'Bookings', icon: Calendar },
  { href: '/provider/reviews', label: 'Reviews', icon: Star },
  { href: '/profile', label: 'Profile', icon: User },
]

const GUIDE_NAV = [
  { href: '/provider/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { href: '/provider/profile', label: 'Guide Profile', icon: MapPin },
  { href: '/provider/bookings', label: 'Bookings', icon: Calendar },
  { href: '/provider/reviews', label: 'Reviews', icon: Star },
  { href: '/profile', label: 'Profile', icon: User },
]

export function AuthNav({ userEmail }: { userEmail: string }) {
  const router = useRouter()
  const pathname = usePathname()
  const { role } = useUserRole()
  const initials = userEmail.slice(0, 2).toUpperCase()
  const [mobileOpen, setMobileOpen] = useState(false)

  let navItems = TOURIST_NAV
  if (role === 'HOTEL_OWNER') navItems = HOTEL_OWNER_NAV
  else if (role === 'TOUR_GUIDE') navItems = GUIDE_NAV
  else if (role === 'VEHICLE_OWNER') navItems = PROVIDER_NAV

  const roleLabel = role === 'HOTEL_OWNER' ? 'Hotel Owner'
    : role === 'TOUR_GUIDE' ? 'Tour Guide'
    : role === 'VEHICLE_OWNER' ? 'Vehicle Owner'
    : 'Tourist'

  async function handleSignOut() {
    const supabase = createClient()
    await supabase.auth.signOut()
    router.push('/')
    router.refresh()
  }

  return (
    <header className="sticky top-0 z-50 border-b border-border/50 bg-white/80 backdrop-blur-xl">
      <nav className="max-w-7xl mx-auto px-4 h-14 flex items-center justify-between">
        <div className="flex items-center gap-8">
          <Link href="/" className="flex items-center gap-2">
            <div className="h-8 w-8 rounded-lg bg-primary flex items-center justify-center">
              <Compass className="h-4.5 w-4.5 text-white" />
            </div>
            <span className="font-display text-lg font-bold text-foreground">
              Travel<span className="text-primary">Plan</span>
            </span>
          </Link>

          <div className="hidden md:flex items-center gap-1">
            {navItems.map(({ href, label, icon: Icon }) => (
              <Link
                key={href}
                href={href}
                className={cn(
                  'flex items-center gap-2 px-3 py-1.5 rounded-lg text-sm font-medium transition-all duration-200',
                  pathname === href || pathname.startsWith(href + '/')
                    ? 'bg-primary/10 text-primary'
                    : 'text-muted-foreground hover:text-foreground hover:bg-accent'
                )}
              >
                <Icon className="h-4 w-4" />
                {label}
              </Link>
            ))}
          </div>
        </div>

        <div className="flex items-center gap-2">
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" className="relative h-9 w-9 rounded-full hidden md:inline-flex">
                <Avatar className="h-9 w-9">
                  <AvatarFallback className="bg-primary/10 text-primary text-xs font-semibold">
                    {initials}
                  </AvatarFallback>
                </Avatar>
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent className="w-56" align="end">
              <div className="px-3 py-2">
                <p className="text-sm font-medium truncate">{userEmail}</p>
                <p className="text-xs text-muted-foreground">{roleLabel}</p>
              </div>
              <DropdownMenuSeparator />
              <DropdownMenuItem asChild>
                <Link href="/profile" className="flex items-center gap-2">
                  <User className="h-4 w-4" />
                  Profile
                </Link>
              </DropdownMenuItem>
              <DropdownMenuItem asChild>
                <Link href="/wallet" className="flex items-center gap-2">
                  <Wallet className="h-4 w-4" />
                  Wallet
                </Link>
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem onClick={handleSignOut} className="text-destructive focus:text-destructive flex items-center gap-2">
                <LogOut className="h-4 w-4" />
                Sign Out
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>

          <Sheet open={mobileOpen} onOpenChange={setMobileOpen}>
            <SheetTrigger asChild>
              <Button variant="ghost" size="sm" className="md:hidden h-9 w-9 p-0">
                <Menu className="h-5 w-5" />
              </Button>
            </SheetTrigger>
            <SheetContent side="right" className="w-72 pt-10">
              <div className="flex items-center gap-3 mb-6 px-1">
                <Avatar className="h-10 w-10">
                  <AvatarFallback className="bg-primary/10 text-primary font-semibold">
                    {initials}
                  </AvatarFallback>
                </Avatar>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium truncate">{userEmail}</p>
                  <p className="text-xs text-muted-foreground">{roleLabel}</p>
                </div>
              </div>

              <div className="space-y-1">
                {navItems.map(({ href, label, icon: Icon }) => (
                  <Link
                    key={href}
                    href={href}
                    onClick={() => setMobileOpen(false)}
                    className={cn(
                      'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors',
                      pathname === href || pathname.startsWith(href + '/')
                        ? 'bg-primary/10 text-primary'
                        : 'text-muted-foreground hover:text-foreground hover:bg-accent'
                    )}
                  >
                    <Icon className="h-5 w-5" />
                    {label}
                  </Link>
                ))}
              </div>

              <div className="absolute bottom-6 left-6 right-6">
                <Button
                  variant="outline"
                  className="w-full justify-start gap-2 text-destructive border-destructive/20 hover:bg-destructive/5 hover:text-destructive"
                  onClick={() => {
                    setMobileOpen(false)
                    handleSignOut()
                  }}
                >
                  <LogOut className="h-4 w-4" />
                  Sign Out
                </Button>
              </div>
            </SheetContent>
          </Sheet>
        </div>
      </nav>
    </header>
  )
}

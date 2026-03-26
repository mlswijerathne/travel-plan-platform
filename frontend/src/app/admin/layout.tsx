import { createClient } from '@/lib/supabase/server'
import { redirect } from 'next/navigation'
import Link from 'next/link'
import {
  LayoutDashboard,
  Package,
  Ticket,
  ShoppingBag,
  ClipboardList,
  LogOut,
  ChevronRight,
  HelpCircle,
} from 'lucide-react'
import { Button } from '@/components/ui/button'

const NAV_ITEMS = [
  { href: '/admin', label: 'Dashboard', icon: LayoutDashboard, exact: true },
  { href: '/admin/packages', label: 'Packages', icon: Package },
  { href: '/admin/events', label: 'Events', icon: Ticket },
  { href: '/admin/products', label: 'Products', icon: ShoppingBag },
  { href: '/admin/orders', label: 'Orders', icon: ClipboardList },
]

export default async function AdminLayout({ children }: { children: React.ReactNode }) {
  const supabase = await createClient()
  const { data: { user } } = await supabase.auth.getUser()

  if (!user) redirect('/login')

  const { data: { session } } = await supabase.auth.getSession()
  let role = 'TOURIST'
  if (session?.access_token) {
    try {
      const payload = JSON.parse(atob(session.access_token.split('.')[1]))
      role = payload.user_metadata?.role || payload.app_metadata?.role || 'TOURIST'
    } catch { /* */ }
  }

  if (role !== 'ADMIN') redirect('/')

  return (
    <div className="min-h-screen flex bg-background">
      {/* Sidebar - Dark Teal */}
      <aside className="w-64 fixed left-0 top-0 h-screen bg-[hsl(175,100%,12%)] shadow-2xl shadow-primary/10 flex flex-col py-6 font-display z-40">
        <div className="px-6 mb-8">
          <Link href="/admin" className="block">
            <h2 className="text-xl font-bold text-white tracking-tight">TravelPlan</h2>
            <p className="text-[10px] uppercase tracking-widest text-white/50 mt-1">Admin Panel</p>
          </Link>
        </div>

        <nav className="flex-1 space-y-1">
          {NAV_ITEMS.map(({ href, label, icon: Icon }) => (
            <AdminNavLink key={href} href={href} label={label} Icon={Icon} />
          ))}
        </nav>

        <div className="px-4 space-y-2">
          <Link
            href="#"
            className="flex items-center gap-3 px-4 py-3 text-white/50 hover:text-white transition-colors"
          >
            <HelpCircle className="h-4 w-4" />
            <span className="text-sm font-medium">Help Center</span>
          </Link>

          <div className="border-t border-white/10 pt-3 px-2">
            <p className="text-xs text-white/40 truncate mb-2">{user.email}</p>
            <form action="/auth/signout" method="post">
              <Button variant="ghost" size="sm" className="w-full justify-start gap-2 text-white/50 hover:text-white hover:bg-white/10">
                <LogOut className="h-4 w-4" />
                Sign Out
              </Button>
            </form>
          </div>
        </div>
      </aside>

      {/* Main content */}
      <main className="flex-1 ml-64 overflow-auto">
        <div className="max-w-6xl mx-auto px-8 py-10">
          {children}
        </div>
      </main>
    </div>
  )
}

function AdminNavLink({
  href,
  label,
  Icon,
}: {
  href: string
  label: string
  Icon: React.ElementType
}) {
  return (
    <Link
      href={href}
      className="flex items-center gap-3 px-4 py-3 mx-2 rounded-lg text-sm font-medium text-white/70 hover:text-white hover:bg-white/10 transition-all duration-200 group"
    >
      <Icon className="h-4 w-4 shrink-0" />
      <span className="flex-1">{label}</span>
      <ChevronRight className="h-3 w-3 opacity-0 group-hover:opacity-50 transition-opacity" />
    </Link>
  )
}

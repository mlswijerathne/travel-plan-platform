'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'
import { getPackages } from '@/lib/api/packages'
import { getMyAdminEvents } from '@/lib/api/events'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Package, Ticket, Plus, ArrowRight, TrendingUp, Users } from 'lucide-react'
import { Skeleton } from '@/components/ui/skeleton'
import { toast } from 'sonner'

export default function AdminDashboardPage() {
  const [stats, setStats] = useState<{
    totalPackages: number
    totalEvents: number
    featuredPackages: number
    publishedEvents: number
  } | null>(null)
  const [recentPackages, setRecentPackages] = useState<any[]>([])
  const [recentEvents, setRecentEvents] = useState<any[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    async function load() {
      try {
        const [pkgRes, evtRes] = await Promise.allSettled([
          getPackages({ page: 0, size: 5 }),
          getMyAdminEvents({ page: 0, size: 5 }),
        ])

        const packages = pkgRes.status === 'fulfilled' ? pkgRes.value.data ?? [] : []
        const events = evtRes.status === 'fulfilled' ? evtRes.value.data ?? [] : []

        setRecentPackages(packages)
        setRecentEvents(events)
        setStats({
          totalPackages: pkgRes.status === 'fulfilled' ? (pkgRes.value.pagination?.totalItems ?? packages.length) : 0,
          totalEvents: evtRes.status === 'fulfilled' ? (evtRes.value.pagination?.totalItems ?? events.length) : 0,
          featuredPackages: packages.filter((p: any) => p.isFeatured).length,
          publishedEvents: events.filter((e: any) => e.status === 'PUBLISHED').length,
        })
      } catch {
        toast.error('Failed to load dashboard data')
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [])

  if (loading) {
    return (
      <div className="space-y-6">
        <Skeleton className="h-8 w-48" />
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {[1, 2, 3, 4].map(i => <Skeleton key={i} className="h-28 rounded-xl" />)}
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <Skeleton className="h-64 rounded-xl" />
          <Skeleton className="h-64 rounded-xl" />
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-8">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="font-display text-2xl font-bold text-foreground">Admin Dashboard</h1>
          <p className="text-muted-foreground mt-1">Manage packages, events, and platform content</p>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {[
          { label: 'Total Packages', value: stats?.totalPackages ?? 0, icon: Package, color: 'text-blue-600', bg: 'bg-blue-50' },
          { label: 'Featured Packages', value: stats?.featuredPackages ?? 0, icon: TrendingUp, color: 'text-emerald-600', bg: 'bg-emerald-50' },
          { label: 'Total Events', value: stats?.totalEvents ?? 0, icon: Ticket, color: 'text-violet-600', bg: 'bg-violet-50' },
          { label: 'Published Events', value: stats?.publishedEvents ?? 0, icon: Users, color: 'text-amber-600', bg: 'bg-amber-50' },
        ].map(({ label, value, icon: Icon, color, bg }) => (
          <Card key={label} className="shadow-sm">
            <CardContent className="flex items-center gap-4 py-5">
              <div className={`h-11 w-11 rounded-xl ${bg} flex items-center justify-center`}>
                <Icon className={`h-5 w-5 ${color}`} />
              </div>
              <div>
                <p className="text-2xl font-bold text-foreground">{value}</p>
                <p className="text-xs text-muted-foreground">{label}</p>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Recent content */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Recent Packages */}
        <Card className="shadow-sm">
          <CardHeader className="flex flex-row items-center justify-between pb-3">
            <CardTitle className="text-base font-semibold">Recent Packages</CardTitle>
            <div className="flex items-center gap-2">
              <Link href="/admin/packages/new">
                <Button size="sm" className="h-8 gap-1.5 text-xs">
                  <Plus className="h-3.5 w-3.5" />
                  New
                </Button>
              </Link>
              <Link href="/admin/packages">
                <Button variant="ghost" size="sm" className="h-8 gap-1 text-xs text-muted-foreground">
                  View all <ArrowRight className="h-3 w-3" />
                </Button>
              </Link>
            </div>
          </CardHeader>
          <CardContent className="space-y-2">
            {recentPackages.length === 0 ? (
              <div className="text-center py-6 text-muted-foreground text-sm">
                No packages yet.{' '}
                <Link href="/admin/packages/new" className="text-primary hover:underline">Create one</Link>
              </div>
            ) : (
              recentPackages.map((pkg: any) => (
                <Link key={pkg.id} href={`/admin/packages/${pkg.id}/edit`}
                  className="flex items-center gap-3 p-2.5 rounded-lg hover:bg-accent transition-colors">
                  <Package className="h-4 w-4 text-muted-foreground shrink-0" />
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium truncate">{pkg.name}</p>
                    <p className="text-xs text-muted-foreground">{pkg.durationDays} days · Rs {pkg.basePrice?.toLocaleString()}</p>
                  </div>
                  {pkg.isFeatured && <Badge variant="secondary" className="text-xs shrink-0">Featured</Badge>}
                </Link>
              ))
            )}
          </CardContent>
        </Card>

        {/* Recent Events */}
        <Card className="shadow-sm">
          <CardHeader className="flex flex-row items-center justify-between pb-3">
            <CardTitle className="text-base font-semibold">Recent Events</CardTitle>
            <div className="flex items-center gap-2">
              <Link href="/admin/events/new">
                <Button size="sm" className="h-8 gap-1.5 text-xs">
                  <Plus className="h-3.5 w-3.5" />
                  New
                </Button>
              </Link>
              <Link href="/admin/events">
                <Button variant="ghost" size="sm" className="h-8 gap-1 text-xs text-muted-foreground">
                  View all <ArrowRight className="h-3 w-3" />
                </Button>
              </Link>
            </div>
          </CardHeader>
          <CardContent className="space-y-2">
            {recentEvents.length === 0 ? (
              <div className="text-center py-6 text-muted-foreground text-sm">
                No events yet.{' '}
                <Link href="/admin/events/new" className="text-primary hover:underline">Create one</Link>
              </div>
            ) : (
              recentEvents.map((evt: any) => (
                <Link key={evt.id} href={`/admin/events/${evt.id}/edit`}
                  className="flex items-center gap-3 p-2.5 rounded-lg hover:bg-accent transition-colors">
                  <Ticket className="h-4 w-4 text-muted-foreground shrink-0" />
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium truncate">{evt.title}</p>
                    <p className="text-xs text-muted-foreground">{evt.location} · {evt.availableSeats ?? '?'} seats left</p>
                  </div>
                  <Badge
                    variant="outline"
                    className={`text-xs shrink-0 ${evt.status === 'PUBLISHED' ? 'border-emerald-300 text-emerald-700 bg-emerald-50' : 'border-gray-200 text-gray-500'}`}
                  >
                    {evt.status}
                  </Badge>
                </Link>
              ))
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

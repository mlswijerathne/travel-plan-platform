'use client'

import { useMyHotels } from '@/hooks/use-hotels'
import { useMyGuideProfile } from '@/hooks/use-guides'
import { useUserRole } from '@/hooks/use-user-role'
import { StatCard } from '@/components/provider/StatCard'
import { Hotel, MapPin, Calendar, Star } from 'lucide-react'
import Link from 'next/link'
import { Button } from '@/components/ui/button'

export default function ProviderDashboardPage() {
  const { role, isLoading: roleLoading } = useUserRole()
  const { data: hotelsData } = useMyHotels({ size: 5 })
  const { data: guideData } = useMyGuideProfile()

  if (roleLoading) {
    return (
      <div className="animate-pulse space-y-6">
        <div className="h-8 bg-muted rounded w-48" />
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {[1, 2, 3, 4].map((i) => <div key={i} className="h-24 bg-muted rounded-xl" />)}
        </div>
      </div>
    )
  }

  const hotels = hotelsData?.data ?? []
  const guide = guideData?.data

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-bold">Dashboard</h1>
        <p className="text-muted-foreground">
          Welcome back! Here&apos;s an overview of your {role === 'HOTEL_OWNER' ? 'properties' : 'guide profile'}.
        </p>
      </div>

      {role === 'HOTEL_OWNER' && (
        <>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <StatCard icon={Hotel} label="Total Hotels" value={hotels.length} />
            <StatCard icon={Calendar} label="Active" value={hotels.filter(h => h.isActive).length} />
            <StatCard icon={Star} label="Avg Rating" value={hotels.length > 0 ? (hotels.reduce((a, h) => a + h.averageRating, 0) / hotels.length).toFixed(1) : '—'} />
            <StatCard icon={MapPin} label="Total Rooms" value={hotels.reduce((a, h) => a + (h.rooms?.length ?? 0), 0)} />
          </div>

          <div>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-semibold">My Hotels</h2>
              <Link href="/provider/hotels">
                <Button variant="outline" size="sm">View All</Button>
              </Link>
            </div>
            {hotels.length === 0 ? (
              <div className="text-center py-8 border rounded-xl">
                <p className="text-muted-foreground mb-2">No hotels yet</p>
                <Link href="/provider/hotels/new">
                  <Button size="sm">Add Your First Hotel</Button>
                </Link>
              </div>
            ) : (
              <div className="space-y-2">
                {hotels.slice(0, 5).map((hotel) => (
                  <Link key={hotel.id} href={`/provider/hotels/${hotel.id}`} className="flex items-center gap-3 p-3 rounded-lg border bg-card hover:bg-accent transition-colors">
                    <span className="text-xl">🏨</span>
                    <div className="flex-1 min-w-0">
                      <p className="font-medium text-sm truncate">{hotel.name}</p>
                      <p className="text-xs text-muted-foreground">{hotel.city}</p>
                    </div>
                    <div className="text-right text-sm">
                      {hotel.reviewCount > 0 && (
                        <div className="flex items-center gap-1">
                          <Star className="h-3 w-3 fill-amber-400 text-amber-400" />
                          <span className="font-medium">{hotel.averageRating.toFixed(1)}</span>
                        </div>
                      )}
                    </div>
                  </Link>
                ))}
              </div>
            )}
          </div>
        </>
      )}

      {role === 'TOUR_GUIDE' && (
        <>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <StatCard icon={Star} label="Rating" value={guide ? guide.averageRating.toFixed(1) : '—'} />
            <StatCard icon={Calendar} label="Reviews" value={guide?.reviewCount ?? 0} />
            <StatCard icon={MapPin} label="Experience" value={guide?.experienceYears ? `${guide.experienceYears}yr` : '—'} />
            <StatCard icon={Hotel} label="Status" value={guide?.isVerified ? 'Verified' : 'Pending'} />
          </div>

          {guide ? (
            <div className="rounded-xl border bg-card p-6">
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-lg font-semibold">Your Profile</h2>
                <Link href="/provider/profile">
                  <Button variant="outline" size="sm">Edit Profile</Button>
                </Link>
              </div>
              <div className="grid grid-cols-2 gap-4 text-sm">
                <div><span className="text-muted-foreground">Name:</span> {guide.firstName} {guide.lastName}</div>
                <div><span className="text-muted-foreground">Email:</span> {guide.email}</div>
                <div><span className="text-muted-foreground">Languages:</span> {guide.languages.join(', ') || '—'}</div>
                <div><span className="text-muted-foreground">Specializations:</span> {guide.specializations.join(', ') || '—'}</div>
              </div>
            </div>
          ) : (
            <div className="text-center py-8 border rounded-xl">
              <p className="text-muted-foreground mb-2">Set up your guide profile to start receiving bookings</p>
              <Link href="/provider/profile">
                <Button size="sm">Create Guide Profile</Button>
              </Link>
            </div>
          )}
        </>
      )}
    </div>
  )
}

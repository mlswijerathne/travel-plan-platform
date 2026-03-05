'use client'

import { useState } from 'react'
import { useMyHotels } from '@/hooks/use-hotels'
import { useMyGuideProfile } from '@/hooks/use-guides'
import { useMyVehicles } from '@/hooks/use-vehicles'
import { useProviderBookings } from '@/hooks/use-bookings'
import { useUserRole } from '@/hooks/use-user-role'
import { BookingCard } from '@/components/bookings/BookingCard'
import { Pagination } from '@/components/shared/Pagination'
import { EmptyState } from '@/components/shared/EmptyState'
import { Button } from '@/components/ui/button'
import { Calendar } from 'lucide-react'
import { BOOKING_STATUSES, type BookingStatus } from '@/types/booking'

export default function ProviderBookingsPage() {
  const { role } = useUserRole()
  const [page, setPage] = useState(0)
  const [statusFilter, setStatusFilter] = useState<BookingStatus | 'ALL'>('ALL')

  const { data: hotelsData } = useMyHotels()
  const { data: guideData } = useMyGuideProfile({ enabled: role === 'TOUR_GUIDE' })
  const { data: vehiclesData } = useMyVehicles()

  const firstHotelId = hotelsData?.data?.[0]?.id
  const guideId = guideData?.data?.id
  const firstVehicleId = vehiclesData?.[0]?.id

  const providerType = role === 'HOTEL_OWNER' ? 'HOTEL' : role === 'VEHICLE_OWNER' ? 'VEHICLE' : 'TOUR_GUIDE'
  const providerId = role === 'HOTEL_OWNER' ? firstHotelId : role === 'VEHICLE_OWNER' ? firstVehicleId : guideId

  const { data, isLoading } = useProviderBookings(
    providerType,
    providerId ?? 0,
    {
      status: statusFilter === 'ALL' ? undefined : statusFilter,
      page,
      size: 10,
    }
  )

  const bookings = data?.data ?? []
  const pagination = data?.pagination

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Provider Bookings</h1>
        <p className="text-muted-foreground">Bookings for your {role === 'HOTEL_OWNER' ? 'hotels' : role === 'VEHICLE_OWNER' ? 'vehicles' : 'guide services'}</p>
      </div>

      <div className="flex flex-wrap gap-1.5">
        {BOOKING_STATUSES.map(({ value, label }) => (
          <Button
            key={value}
            variant={statusFilter === value ? 'default' : 'outline'}
            size="sm"
            onClick={() => { setStatusFilter(value); setPage(0) }}
          >
            {label}
          </Button>
        ))}
      </div>

      {isLoading ? (
        <div className="space-y-3">
          {[1, 2, 3].map((i) => (
            <div key={i} className="rounded-xl border bg-card p-4 animate-pulse">
              <div className="h-5 bg-muted rounded w-48 mb-2" />
              <div className="h-4 bg-muted rounded w-32" />
            </div>
          ))}
        </div>
      ) : !providerId ? (
        <EmptyState
          icon={Calendar}
          title="No provider profile"
          description="Set up your provider profile first to see bookings."
        />
      ) : bookings.length === 0 ? (
        <EmptyState
          icon={Calendar}
          title="No bookings found"
          description="Bookings from tourists will appear here."
        />
      ) : (
        <>
          <div className="space-y-3">
            {bookings.map((booking) => (
              <BookingCard key={booking.id} booking={booking} />
            ))}
          </div>
          {pagination && (
            <Pagination page={page} totalPages={pagination.totalPages} onPageChange={setPage} />
          )}
        </>
      )}
    </div>
  )
}

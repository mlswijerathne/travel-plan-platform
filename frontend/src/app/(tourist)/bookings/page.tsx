'use client'

import { useState } from 'react'
import { useMyBookings } from '@/hooks/use-bookings'
import { BookingCard } from '@/components/bookings/BookingCard'
import { Pagination } from '@/components/shared/Pagination'
import { EmptyState } from '@/components/shared/EmptyState'
import { Button } from '@/components/ui/button'
import { Calendar, Plus } from 'lucide-react'
import { BOOKING_STATUSES, type BookingStatus } from '@/types/booking'
import Link from 'next/link'

export default function BookingsPage() {
  const [page, setPage] = useState(0)
  const [statusFilter, setStatusFilter] = useState<BookingStatus | 'ALL'>('ALL')

  const { data, isLoading } = useMyBookings({
    status: statusFilter === 'ALL' ? undefined : statusFilter,
    page,
    size: 10,
  })

  const bookings = data?.data ?? []
  const pagination = data?.pagination

  return (
    <div className="space-y-8">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="font-display text-3xl font-extrabold tracking-tight text-foreground">My Bookings</h1>
          <p className="text-muted-foreground mt-1">View and manage your travel bookings</p>
        </div>
        <Link href="/bookings/new">
          <Button>
            <Plus className="h-4 w-4 mr-2" /> New Booking
          </Button>
        </Link>
      </div>

      <div className="bg-surface-low rounded-xl p-1 inline-flex flex-wrap gap-0.5">
        {BOOKING_STATUSES.map(({ value, label }) => (
          <button
            key={value}
            onClick={() => { setStatusFilter(value); setPage(0) }}
            className={`px-4 py-1.5 text-sm font-medium rounded-lg transition-all duration-200 ${
              statusFilter === value
                ? 'bg-card shadow-sm text-foreground'
                : 'text-muted-foreground hover:text-foreground'
            }`}
          >
            {label}
          </button>
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
      ) : bookings.length === 0 ? (
        <EmptyState
          icon={Calendar}
          title="No bookings found"
          description={statusFilter === 'ALL' ? "You haven't made any bookings yet." : `No ${statusFilter.toLowerCase()} bookings.`}
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

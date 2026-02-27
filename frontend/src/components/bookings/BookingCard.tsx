'use client'

import Link from 'next/link'
import { Badge } from '@/components/ui/badge'
import type { Booking } from '@/types/booking'
import { formatCurrency, formatDateRange, getBookingStatusColor, getProviderTypeLabel } from '@/lib/utils'
import { Calendar, Package } from 'lucide-react'

export function BookingCard({ booking }: { booking: Booking }) {
  return (
    <Link href={`/bookings/${booking.id}`} className="group block">
      <div className="rounded-xl border bg-card p-4 transition-all hover:shadow-md hover:border-primary/20">
        <div className="flex items-start justify-between gap-3">
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-2 mb-1">
              <span className="font-mono text-sm font-medium">{booking.bookingReference}</span>
              <Badge className={getBookingStatusColor(booking.status)}>{booking.status}</Badge>
            </div>
            <div className="flex items-center gap-1 text-sm text-muted-foreground">
              <Calendar className="h-3.5 w-3.5" />
              {formatDateRange(booking.startDate, booking.endDate)}
            </div>
          </div>
          <span className="text-lg font-semibold text-primary shrink-0">
            {formatCurrency(booking.totalAmount)}
          </span>
        </div>

        {booking.items.length > 0 && (
          <div className="mt-3 pt-3 border-t">
            <div className="flex items-center gap-1 text-xs text-muted-foreground mb-1.5">
              <Package className="h-3 w-3" />
              {booking.items.length} item{booking.items.length !== 1 ? 's' : ''}
            </div>
            <div className="flex flex-wrap gap-1">
              {booking.items.slice(0, 3).map((item) => (
                <Badge key={item.id} variant="outline" className="text-[10px]">
                  {getProviderTypeLabel(item.providerType)}: {item.itemName}
                </Badge>
              ))}
              {booking.items.length > 3 && (
                <Badge variant="outline" className="text-[10px]">
                  +{booking.items.length - 3} more
                </Badge>
              )}
            </div>
          </div>
        )}
      </div>
    </Link>
  )
}

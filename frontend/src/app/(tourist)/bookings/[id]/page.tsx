'use client'

import { use } from 'react'
import { useBooking, useCancelBooking } from '@/hooks/use-bookings'
import { BookingItemCard } from '@/components/bookings/BookingItemCard'
import { CancelBookingDialog } from '@/components/bookings/CancelBookingDialog'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { ArrowLeft, Calendar, FileText } from 'lucide-react'
import { formatCurrency, formatDateRange, formatDate, getBookingStatusColor } from '@/lib/utils'
import Link from 'next/link'

export default function BookingDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params)
  const bookingId = Number(id)
  const { data, isLoading } = useBooking(bookingId)
  const cancelMutation = useCancelBooking()
  const booking = data?.data

  if (isLoading) {
    return (
      <div className="animate-pulse space-y-6">
        <div className="h-8 bg-muted rounded w-48" />
        <div className="h-48 bg-muted rounded-xl" />
      </div>
    )
  }

  if (!booking) {
    return (
      <div className="text-center py-16">
        <p className="text-muted-foreground">Booking not found</p>
        <Link href="/bookings">
          <Button variant="outline" className="mt-4">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Bookings
          </Button>
        </Link>
      </div>
    )
  }

  const canCancel = booking.status === 'PENDING' || booking.status === 'CONFIRMED'

  return (
    <div className="space-y-6">
      <Link href="/bookings" className="inline-flex items-center text-sm text-muted-foreground hover:text-foreground">
        <ArrowLeft className="h-4 w-4 mr-1" />
        Back to Bookings
      </Link>

      {/* Header */}
      <div className="rounded-xl border bg-card p-6">
        <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4">
          <div>
            <div className="flex items-center gap-2 mb-2">
              <h1 className="text-xl font-bold font-mono">{booking.bookingReference}</h1>
              <Badge className={getBookingStatusColor(booking.status)}>{booking.status}</Badge>
            </div>
            <div className="flex items-center gap-1 text-sm text-muted-foreground">
              <Calendar className="h-3.5 w-3.5" />
              {formatDateRange(booking.startDate, booking.endDate)}
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              Booked on {formatDate(booking.bookingDate)}
            </p>
          </div>
          <div className="text-right">
            <p className="text-2xl font-bold text-primary">{formatCurrency(booking.totalAmount)}</p>
            {booking.refundPolicy && (
              <p className="text-xs text-muted-foreground">
                Refund: {booking.refundPolicy.replace('_', ' ')}
              </p>
            )}
          </div>
        </div>

        {booking.notes && (
          <div className="mt-4 pt-4 border-t flex items-start gap-2">
            <FileText className="h-4 w-4 text-muted-foreground mt-0.5" />
            <p className="text-sm text-muted-foreground">{booking.notes}</p>
          </div>
        )}

        {booking.cancellationReason && (
          <div className="mt-4 pt-4 border-t">
            <p className="text-sm font-medium text-red-600">Cancellation Reason:</p>
            <p className="text-sm text-muted-foreground">{booking.cancellationReason}</p>
            {booking.refundAmount != null && booking.refundAmount > 0 && (
              <p className="text-sm text-green-600 mt-1">
                Refund: {formatCurrency(booking.refundAmount)}
              </p>
            )}
          </div>
        )}

        {canCancel && (
          <div className="mt-4 pt-4 border-t flex justify-end">
            <CancelBookingDialog
              onConfirm={(reason) => cancelMutation.mutate({ id: bookingId, data: { reason } })}
              isPending={cancelMutation.isPending}
            />
          </div>
        )}
      </div>

      {/* Items */}
      <div>
        <h2 className="text-lg font-semibold mb-4">Booking Items ({booking.items.length})</h2>
        <div className="space-y-3">
          {booking.items.map((item) => (
            <BookingItemCard key={item.id} item={item} bookingId={booking.id} bookingStatus={booking.status} />
          ))}
        </div>
      </div>

      {/* Leave Review Link */}
      {booking.status === 'COMPLETED' && (
        <div className="rounded-xl border bg-primary/5 p-4 text-center">
          <p className="text-sm text-muted-foreground mb-2">How was your experience?</p>
          <Link href="/reviews">
            <Button size="sm">Leave a Review</Button>
          </Link>
        </div>
      )}
    </div>
  )
}

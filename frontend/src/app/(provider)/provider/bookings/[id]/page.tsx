'use client'

import { use, useState } from 'react'
import { useBookingDetails, useUpdateBookingItemStatus } from '@/hooks/use-bookings'
import { useMyHotels } from '@/hooks/use-hotels'
import { useMyGuideProfile } from '@/hooks/use-guides'
import { useMyVehicles } from '@/hooks/use-vehicles'
import { useUserRole } from '@/hooks/use-user-role'
import { useReviewsByEntity } from '@/hooks/use-reviews'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { ArrowLeft, Calendar, Hotel, Car, MapPin, Star } from 'lucide-react'
import { formatCurrency, formatDateRange, formatDate, getBookingStatusColor, getProviderTypeLabel } from '@/lib/utils'
import type { BookingItem } from '@/types/booking'
import type { EntityType } from '@/types/review'
import Link from 'next/link'

const PROVIDER_ICONS = {
  HOTEL: Hotel,
  TOUR_GUIDE: MapPin,
  VEHICLE: Car,
} as const

function ProviderBookingItemRow({
  item,
  bookingId,
  isMyItem,
  bookingStatus,
}: {
  item: BookingItem
  bookingId: number
  isMyItem: boolean
  bookingStatus: string
}) {
  const [declineReason, setDeclineReason] = useState('')
  const [showDeclineInput, setShowDeclineInput] = useState(false)
  const updateStatus = useUpdateBookingItemStatus()
  const Icon = PROVIDER_ICONS[item.providerType] ?? Hotel

  const showReviews = isMyItem && bookingStatus === 'COMPLETED'
  const { data: reviewsData } = useReviewsByEntity(
    item.providerType as EntityType,
    item.providerId,
    {},
    showReviews
  )
  const reviews = showReviews
    ? (reviewsData?.data?.data ?? []).filter((r) => r.bookingId === bookingId)
    : []

  function handleAccept() {
    updateStatus.mutate({ bookingId, itemId: item.id, status: 'CONFIRMED' })
  }

  function handleDecline() {
    if (!showDeclineInput) {
      setShowDeclineInput(true)
      return
    }
    updateStatus.mutate({ bookingId, itemId: item.id, status: 'CANCELLED', reason: declineReason || undefined })
    setShowDeclineInput(false)
  }

  return (
    <div className={`rounded-lg border bg-card p-4 ${!isMyItem ? 'opacity-60' : ''}`}>
      <div className="flex gap-4">
        <div className="h-10 w-10 rounded-lg bg-primary/10 flex items-center justify-center shrink-0">
          <Icon className="h-5 w-5 text-primary" />
        </div>
        <div className="flex-1 min-w-0">
          <div className="flex items-start justify-between gap-2">
            <div>
              <p className="font-medium text-sm">{item.itemName}</p>
              <Badge variant="outline" className="text-[10px] mt-0.5">
                {getProviderTypeLabel(item.providerType)}
              </Badge>
              {!isMyItem && (
                <span className="ml-2 text-[10px] text-muted-foreground">(other provider)</span>
              )}
            </div>
            <div className="text-right shrink-0 space-y-1.5">
              <p className="font-semibold text-sm">{formatCurrency(item.subtotal)}</p>
              <Badge className={`text-[10px] ${getBookingStatusColor(item.status)}`}>{item.status}</Badge>
            </div>
          </div>
          <div className="flex items-center gap-3 mt-2 text-xs text-muted-foreground">
            <span>Qty: {item.quantity}</span>
            <span>{formatCurrency(item.unitPrice)} each</span>
            {item.startDate && item.endDate && (
              <span>{formatDateRange(item.startDate, item.endDate)}</span>
            )}
          </div>

          {isMyItem && item.status === 'PENDING' && (
            <div className="mt-3 space-y-2">
              {showDeclineInput && (
                <input
                  type="text"
                  className="w-full rounded-md border px-3 py-1.5 text-sm"
                  placeholder="Reason for declining (optional)"
                  value={declineReason}
                  onChange={(e) => setDeclineReason(e.target.value)}
                />
              )}
              <div className="flex gap-2">
                <Button size="sm" onClick={handleAccept} disabled={updateStatus.isPending}>Accept</Button>
                <Button size="sm" variant="destructive" onClick={handleDecline} disabled={updateStatus.isPending}>
                  {showDeclineInput ? 'Confirm Decline' : 'Decline'}
                </Button>
                {showDeclineInput && (
                  <Button size="sm" variant="ghost" onClick={() => { setShowDeclineInput(false); setDeclineReason('') }}>
                    Cancel
                  </Button>
                )}
              </div>
            </div>
          )}

          {/* Inline reviews for completed items */}
          {showReviews && (
            <div className="mt-3 pt-3 border-t space-y-2">
              <p className="text-xs font-medium text-muted-foreground flex items-center gap-1">
                <Star className="h-3 w-3" />
                Tourist Reviews
              </p>
              {reviews.length === 0 ? (
                <p className="text-xs text-muted-foreground">No reviews yet.</p>
              ) : (
                reviews.map((review) => (
                  <div key={review.id} className="rounded-md bg-muted/40 px-3 py-2">
                    <div className="flex items-center justify-between gap-2">
                      <span className="text-sm font-medium">
                        {review.touristName || 'Anonymous'}
                      </span>
                      <div className="flex items-center gap-0.5">
                        {Array.from({ length: 5 }).map((_, i) => (
                          <Star
                            key={i}
                            className={`h-3 w-3 ${i < review.rating ? 'fill-amber-400 text-amber-400' : 'text-muted-foreground/30'}`}
                          />
                        ))}
                      </div>
                    </div>
                    {review.content && (
                      <p className="text-xs text-muted-foreground mt-1">{review.content}</p>
                    )}
                  </div>
                ))
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default function ProviderBookingDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params)
  const bookingId = Number(id)
  const { data, isLoading } = useBookingDetails(bookingId)
  const booking = data?.data

  const { role } = useUserRole()
  const { data: hotelsData } = useMyHotels()
  const { data: guideData } = useMyGuideProfile({ enabled: role === 'TOUR_GUIDE' })
  const { data: vehiclesData } = useMyVehicles()

  const providerType = role === 'HOTEL_OWNER' ? 'HOTEL' : role === 'VEHICLE_OWNER' ? 'VEHICLE' : 'TOUR_GUIDE'
  const providerId = role === 'HOTEL_OWNER'
    ? hotelsData?.data?.[0]?.id
    : role === 'VEHICLE_OWNER'
    ? vehiclesData?.[0]?.id
    : guideData?.data?.id

  function isMyItem(item: BookingItem): boolean {
    if (!providerId) return false
    return item.providerType === providerType && item.providerId === providerId
  }

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
        <Link href="/provider/bookings">
          <Button variant="outline" className="mt-4">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Bookings
          </Button>
        </Link>
      </div>
    )
  }

  const myItems = booking.items.filter(isMyItem)
  const otherItems = booking.items.filter((item) => !isMyItem(item))
  const hasPendingItems = myItems.some((item) => item.status === 'PENDING')

  return (
    <div className="space-y-6">
      <Link href="/provider/bookings" className="inline-flex items-center text-sm text-muted-foreground hover:text-foreground">
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
          </div>
        </div>

        {booking.notes && (
          <div className="mt-4 pt-4 border-t">
            <p className="text-sm text-muted-foreground">{booking.notes}</p>
          </div>
        )}

        {!hasPendingItems && myItems.length > 0 && (
          <div className="mt-4 pt-4 border-t">
            <p className="text-sm text-muted-foreground">
              All your items for this booking have already been responded to.
            </p>
          </div>
        )}
      </div>

      {/* My Items */}
      {myItems.length > 0 && (
        <div>
          <h2 className="text-lg font-semibold mb-4">Your Items ({myItems.length})</h2>
          <div className="space-y-3">
            {myItems.map((item) => (
              <ProviderBookingItemRow key={item.id} item={item} bookingId={booking.id} isMyItem={true} bookingStatus={booking.status} />
            ))}
          </div>
        </div>
      )}

      {/* Other Items */}
      {otherItems.length > 0 && (
        <div>
          <h2 className="text-base font-medium text-muted-foreground mb-3">Other Items in this Booking</h2>
          <div className="space-y-3">
            {otherItems.map((item) => (
              <ProviderBookingItemRow key={item.id} item={item} bookingId={booking.id} isMyItem={false} bookingStatus={booking.status} />
            ))}
          </div>
        </div>
      )}
    </div>
  )
}


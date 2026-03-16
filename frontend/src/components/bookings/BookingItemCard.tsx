'use client'

import { useState } from 'react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import type { BookingItem } from '@/types/booking'
import type { EntityType, Review } from '@/types/review'
import { formatCurrency, formatDateRange, getBookingStatusColor, getProviderTypeLabel } from '@/lib/utils'
import { Hotel, MapPin, Car, Star, CheckCircle2, Pencil } from 'lucide-react'
import { ReviewFormDialog } from '@/components/reviews/ReviewFormDialog'

const PROVIDER_ICONS = {
  HOTEL: Hotel,
  TOUR_GUIDE: MapPin,
  VEHICLE: Car,
} as const

const ENTITY_TYPE_MAP: Record<string, EntityType> = {
  HOTEL: 'HOTEL',
  TOUR_GUIDE: 'TOUR_GUIDE',
  VEHICLE: 'VEHICLE',
}

interface BookingItemCardProps {
  item: BookingItem
  bookingId?: number
  bookingStatus?: string
  existingReviews?: Review[]
}

export function BookingItemCard({ item, bookingId, bookingStatus, existingReviews }: BookingItemCardProps) {
  const [dialogOpen, setDialogOpen] = useState(false)
  const [editOpen, setEditOpen] = useState(false)
  const Icon = PROVIDER_ICONS[item.providerType] ?? Hotel
  const canReview = !!bookingId && bookingStatus === 'COMPLETED'

  const myReview = canReview
    ? existingReviews?.find(
        (r) => r.entityType === ENTITY_TYPE_MAP[item.providerType] && r.entityId === item.providerId
      )
    : undefined
  const alreadyReviewed = !!myReview

  return (
    <div className="rounded-lg border bg-card p-4 flex gap-4">
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
          </div>
          <div className="text-right shrink-0 space-y-1.5">
            <p className="font-semibold text-sm">{formatCurrency(item.subtotal)}</p>
            <Badge className={`text-[10px] ${getBookingStatusColor(item.status)}`}>{item.status}</Badge>
            {canReview && (
              <div>
                {alreadyReviewed ? (
                  <div className="flex items-center gap-1 text-xs text-emerald-600 font-medium">
                    <CheckCircle2 className="h-3.5 w-3.5" />
                    Reviewed
                  </div>
                ) : (
                  <Button
                    size="sm"
                    variant="outline"
                    className="h-7 text-xs"
                    onClick={() => setDialogOpen(true)}
                  >
                    <Star className="h-3 w-3 mr-1" />
                    Review
                  </Button>
                )}
              </div>
            )}
          </div>
        </div>
        <div className="flex items-center gap-3 mt-2 text-xs text-muted-foreground">
          <span>Qty: {item.quantity}</span>
          <span>{formatCurrency(item.unitPrice)} each</span>
          {item.startDate && item.endDate && (
            <span>{formatDateRange(item.startDate, item.endDate)}</span>
          )}
        </div>

        {myReview && (
          <div className="mt-3 pt-3 border-t">
            <div className="flex items-center justify-between gap-2 mb-1">
              <div className="flex items-center gap-0.5">
                {Array.from({ length: 5 }).map((_, i) => (
                  <Star
                    key={i}
                    className={`h-3 w-3 ${i < myReview.rating ? 'fill-amber-400 text-amber-400' : 'text-muted-foreground/30'}`}
                  />
                ))}
              </div>
              <Button variant="ghost" size="sm" className="h-6 w-6 p-0" onClick={() => setEditOpen(true)}>
                <Pencil className="h-3 w-3" />
              </Button>
            </div>
            {myReview.content && (
              <p className="text-xs text-muted-foreground">{myReview.content}</p>
            )}
          </div>
        )}
      </div>

      {myReview && editOpen && (
        <ReviewFormDialog
          open={editOpen}
          onOpenChange={setEditOpen}
          editReview={myReview}
        />
      )}

      {canReview && !alreadyReviewed && (
        <ReviewFormDialog
          open={dialogOpen}
          onOpenChange={setDialogOpen}
          entityType={ENTITY_TYPE_MAP[item.providerType] ?? 'HOTEL'}
          entityId={item.providerId}
          bookingId={bookingId}
          entityName={item.itemName}
        />
      )}
    </div>
  )
}

'use client'

import { useState } from 'react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import type { BookingItem } from '@/types/booking'
import type { EntityType } from '@/types/review'
import { formatCurrency, formatDateRange, getBookingStatusColor, getProviderTypeLabel } from '@/lib/utils'
import { Hotel, MapPin, Car, Star } from 'lucide-react'
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
}

export function BookingItemCard({ item, bookingId, bookingStatus }: BookingItemCardProps) {
  const [dialogOpen, setDialogOpen] = useState(false)
  const Icon = PROVIDER_ICONS[item.providerType] ?? Hotel
  const canReview = !!bookingId && (bookingStatus === 'CONFIRMED' || bookingStatus === 'COMPLETED')

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
                <Button
                  size="sm"
                  variant="outline"
                  className="h-7 text-xs"
                  onClick={() => setDialogOpen(true)}
                >
                  <Star className="h-3 w-3 mr-1" />
                  Review
                </Button>
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
      </div>

      {canReview && (
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

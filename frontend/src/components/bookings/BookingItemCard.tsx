'use client'

import { Badge } from '@/components/ui/badge'
import type { BookingItem } from '@/types/booking'
import { formatCurrency, formatDateRange, getBookingStatusColor, getProviderTypeLabel } from '@/lib/utils'
import { Hotel, MapPin, Car } from 'lucide-react'

const PROVIDER_ICONS = {
  HOTEL: Hotel,
  TOUR_GUIDE: MapPin,
  VEHICLE: Car,
} as const

export function BookingItemCard({ item }: { item: BookingItem }) {
  const Icon = PROVIDER_ICONS[item.providerType] ?? Hotel

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
          <div className="text-right shrink-0">
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
      </div>
    </div>
  )
}

'use client'

import type { ProviderResult } from '@/types/chat'
import { Card, CardContent } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { formatCurrency } from '@/lib/utils'
import { Star, MapPin, ShieldCheck, Building2, User, Car, CalendarDays, ShoppingBag, ExternalLink, ShoppingCart } from 'lucide-react'

const TYPE_ICONS: Record<string, typeof Building2> = {
  HOTEL: Building2,
  TOUR_GUIDE: User,
  VEHICLE: Car,
  EVENT: CalendarDays,
  PRODUCT: ShoppingBag,
}

const TYPE_ROUTES: Record<string, string> = {
  HOTEL: '/hotels',
  TOUR_GUIDE: '/guides',
  VEHICLE: '/vehicles',
  EVENT: '/events',
}

interface ProviderCardProps {
  provider: ProviderResult
  onAddToBooking?: (provider: ProviderResult) => void
}

export function ProviderCard({ provider, onAddToBooking }: ProviderCardProps) {
  const TypeIcon = TYPE_ICONS[provider.type] ?? Building2
  const isInternal = provider.source === 'INTERNAL'
  const detailRoute = TYPE_ROUTES[provider.type]
  const detailUrl = isInternal && detailRoute && provider.id ? `${detailRoute}/${provider.id}` : null

  function handleCardClick(e: React.MouseEvent) {
    // Don't navigate if clicking a button
    if ((e.target as HTMLElement).closest('button')) return
    if (detailUrl) {
      window.open(detailUrl, '_blank', 'noopener,noreferrer')
    }
  }

  return (
    <Card
      className={`overflow-hidden hover:shadow-md hover:-translate-y-0.5 transition-all duration-200 border-border/50 ${detailUrl ? 'cursor-pointer' : ''}`}
      onClick={handleCardClick}
    >
      {/* Image or gradient placeholder */}
      {provider.imageUrl ? (
        <div className="h-32 bg-muted overflow-hidden">
          <img
            src={provider.imageUrl}
            alt={provider.name}
            className="w-full h-full object-cover"
          />
        </div>
      ) : (
        <div className="h-24 bg-gradient-to-br from-primary/10 via-teal-50 to-emerald-50 flex items-center justify-center">
          <TypeIcon className="h-8 w-8 text-primary/40" />
        </div>
      )}

      <CardContent className="p-3.5 space-y-2.5">
        <div className="flex items-start justify-between gap-2">
          <div className="min-w-0">
            <h4 className="font-semibold text-sm truncate flex items-center gap-1">
              {provider.name}
              {detailUrl && <ExternalLink className="h-3 w-3 text-muted-foreground shrink-0" />}
            </h4>
            <div className="flex items-center gap-1 text-xs text-muted-foreground mt-0.5">
              <MapPin className="h-3 w-3 shrink-0" />
              <span className="truncate">{provider.location}</span>
            </div>
          </div>
        <Badge
            variant={isInternal ? 'default' : 'outline'}
            className={isInternal
              ? 'text-[10px] shrink-0 gap-1 px-1.5 py-0.5 bg-emerald-600 hover:bg-emerald-700 border-emerald-600'
              : 'text-[10px] shrink-0 gap-1 px-1.5 py-0.5 text-muted-foreground'}
          >
            {isInternal && <ShieldCheck className="h-3 w-3" />}
            {isInternal ? 'Platform Partner' : 'External'}
          </Badge>
        </div>

        {provider.description && (
          <p className="text-xs text-muted-foreground line-clamp-2 leading-relaxed">{provider.description}</p>
        )}

        <div className="flex items-center justify-between pt-1 border-t border-border/50">
          <div className="flex items-center gap-1">
            <Star className="h-3.5 w-3.5 fill-amber-400 text-amber-400" />
            <span className="text-xs font-semibold">{provider.rating?.toFixed(1) ?? 'N/A'}</span>
          </div>
          {provider.price > 0 && (
            <span className="text-sm font-bold text-primary">
              {formatCurrency(provider.price)}
            </span>
          )}
        </div>

        {/* Book button for internal providers */}
        {isInternal && onAddToBooking && (
          <Button
            size="sm"
            variant="outline"
            className="w-full text-xs gap-1.5 h-8 border-primary/30 text-primary hover:bg-primary/5"
            onClick={(e) => {
              e.stopPropagation()
              onAddToBooking(provider)
            }}
          >
            <ShoppingCart className="h-3 w-3" />
            Add to Booking
          </Button>
        )}
      </CardContent>
    </Card>
  )
}

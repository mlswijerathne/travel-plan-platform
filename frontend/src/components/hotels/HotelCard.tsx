'use client'

import Link from 'next/link'
import { MapPin, Star } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import type { Hotel } from '@/types/hotel'
import { formatCurrency, formatRating, formatAmenity } from '@/lib/utils'

export function HotelCard({ hotel }: { hotel: Hotel }) {
  const rooms = hotel.rooms ?? []
  const amenities = hotel.amenities ?? []
  const minPrice = rooms.length > 0
    ? Math.min(...rooms.filter(r => r.isActive).map(r => r.pricePerNight))
    : null

  return (
    <Link href={`/hotels/${hotel.id}`} className="group block">
      <div className="rounded-xl border bg-card overflow-hidden transition-all hover:shadow-md hover:border-primary/20">
        <div className="h-44 bg-gradient-to-br from-primary/10 to-primary/5 flex items-center justify-center">
          <span className="text-4xl">🏨</span>
        </div>
        <div className="p-4">
          <div className="flex items-start justify-between gap-2 mb-1">
            <h3 className="font-semibold text-foreground group-hover:text-primary transition-colors line-clamp-1">
              {hotel.name}
            </h3>
            {hotel.starRating && (
              <div className="flex items-center gap-0.5 shrink-0">
                {Array.from({ length: hotel.starRating }, (_, i) => (
                  <Star key={i} className="h-3 w-3 fill-amber-400 text-amber-400" />
                ))}
              </div>
            )}
          </div>

          <div className="flex items-center gap-1 text-sm text-muted-foreground mb-2">
            <MapPin className="h-3.5 w-3.5" />
            {hotel.city}
          </div>

          <div className="flex items-center gap-3 mb-3">
            {hotel.reviewCount > 0 && (
              <div className="flex items-center gap-1 text-sm">
                <Star className="h-3.5 w-3.5 fill-amber-400 text-amber-400" />
                <span className="font-medium">{formatRating(hotel.averageRating)}</span>
                <span className="text-muted-foreground">({hotel.reviewCount})</span>
              </div>
            )}
            {minPrice !== null && (
              <span className="text-sm font-medium text-primary ml-auto">
                From {formatCurrency(minPrice)}<span className="text-xs font-normal text-muted-foreground">/night</span>
              </span>
            )}
          </div>

          {amenities.length > 0 && (
            <div className="flex flex-wrap gap-1">
              {amenities.slice(0, 4).map((a) => (
                <Badge key={a} variant="secondary" className="text-[10px] px-1.5 py-0">
                  {formatAmenity(a)}
                </Badge>
              ))}
              {amenities.length > 4 && (
                <Badge variant="secondary" className="text-[10px] px-1.5 py-0">
                  +{amenities.length - 4}
                </Badge>
              )}
            </div>
          )}
        </div>
      </div>
    </Link>
  )
}

'use client'

import { Users } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import type { Room } from '@/types/hotel'
import { formatCurrency, formatAmenity } from '@/lib/utils'

export function RoomCard({ room }: { room: Room }) {
  return (
    <div className="rounded-lg border bg-card p-4 flex flex-col sm:flex-row gap-4">
      <div className="h-24 w-full sm:w-32 bg-gradient-to-br from-primary/10 to-primary/5 rounded-lg flex items-center justify-center shrink-0">
        <span className="text-2xl">🛏️</span>
      </div>
      <div className="flex-1 min-w-0">
        <div className="flex items-start justify-between gap-2">
          <div>
            <h4 className="font-medium text-foreground">{room.name}</h4>
            <Badge variant="outline" className="text-[10px] mt-1">{room.roomType}</Badge>
          </div>
          <div className="text-right shrink-0">
            <p className="text-lg font-semibold text-primary">{formatCurrency(room.pricePerNight)}</p>
            <p className="text-xs text-muted-foreground">per night</p>
          </div>
        </div>
        {room.description && (
          <p className="text-sm text-muted-foreground mt-2 line-clamp-2">{room.description}</p>
        )}
        <div className="flex items-center gap-4 mt-2">
          <div className="flex items-center gap-1 text-sm text-muted-foreground">
            <Users className="h-3.5 w-3.5" />
            Up to {room.maxOccupancy} guests
          </div>
          {room.amenities.length > 0 && (
            <div className="flex gap-1">
              {room.amenities.slice(0, 3).map((a) => (
                <Badge key={a} variant="secondary" className="text-[10px] px-1.5 py-0">
                  {formatAmenity(a)}
                </Badge>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

'use client'

import { use, useState } from 'react'
import { useHotelDetails, useHotelAvailability } from '@/hooks/use-hotels'
import { RoomCard } from '@/components/hotels/RoomCard'
import { ReviewSection } from '@/components/shared/ReviewSection'
import { StarRating } from '@/components/shared/StarRating'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { MapPin, Clock, Star, ArrowLeft, CheckCircle, XCircle } from 'lucide-react'
import { formatRating, formatAmenity } from '@/lib/utils'
import Link from 'next/link'

export default function HotelDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params)
  const hotelId = Number(id)
  const { data, isLoading } = useHotelDetails(hotelId)
  const hotel = data?.data

  const [checkIn, setCheckIn] = useState('')
  const [checkOut, setCheckOut] = useState('')
  const { data: availData, isLoading: checkingAvail } = useHotelAvailability(
    hotelId,
    checkIn,
    checkOut
  )
  const availability = availData?.data

  if (isLoading) {
    return (
      <div className="animate-pulse space-y-6">
        <div className="h-8 bg-muted rounded w-48" />
        <div className="h-64 bg-muted rounded-xl" />
        <div className="h-32 bg-muted rounded-xl" />
      </div>
    )
  }

  if (!hotel) {
    return (
      <div className="text-center py-16">
        <p className="text-muted-foreground">Hotel not found</p>
        <Link href="/hotels">
          <Button variant="outline" className="mt-4">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Hotels
          </Button>
        </Link>
      </div>
    )
  }

  const activeRooms = hotel.rooms?.filter(r => r.isActive) ?? []

  return (
    <div className="space-y-8">
      <Link href="/hotels" className="inline-flex items-center text-sm text-muted-foreground hover:text-foreground">
        <ArrowLeft className="h-4 w-4 mr-1" />
        Back to Hotels
      </Link>

      {/* Hero */}
      <div className="rounded-xl border bg-card overflow-hidden">
        <div className="h-56 bg-gradient-to-br from-primary/10 to-primary/5 flex items-center justify-center overflow-hidden">
          {hotel.imageUrl ? (
            <img src={hotel.imageUrl} alt={hotel.name} className="w-full h-full object-cover" />
          ) : (
            <span className="text-6xl">🏨</span>
          )}
        </div>
        <div className="p-6">
          <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4">
            <div>
              <h1 className="text-2xl font-bold">{hotel.name}</h1>
              <div className="flex items-center gap-3 mt-2">
                <div className="flex items-center gap-1 text-sm text-muted-foreground">
                  <MapPin className="h-4 w-4" />
                  {hotel.address}, {hotel.city}
                </div>
                {hotel.starRating && (
                  <StarRating rating={hotel.starRating} size="sm" />
                )}
              </div>
            </div>
            {hotel.reviewCount > 0 && (
              <div className="flex items-center gap-2 bg-primary/5 rounded-lg px-3 py-2">
                <Star className="h-5 w-5 fill-amber-400 text-amber-400" />
                <span className="text-lg font-bold">{formatRating(hotel.averageRating)}</span>
                <span className="text-sm text-muted-foreground">({hotel.reviewCount} reviews)</span>
              </div>
            )}
          </div>

          {hotel.description && (
            <p className="text-muted-foreground mt-4">{hotel.description}</p>
          )}

          <div className="flex items-center gap-4 mt-4 text-sm text-muted-foreground">
            <div className="flex items-center gap-1">
              <Clock className="h-3.5 w-3.5" />
              Check-in: {hotel.checkInTime}
            </div>
            <div className="flex items-center gap-1">
              <Clock className="h-3.5 w-3.5" />
              Check-out: {hotel.checkOutTime}
            </div>
          </div>

          {hotel.amenities.length > 0 && (
            <div className="flex flex-wrap gap-1.5 mt-4">
              {hotel.amenities.map((a) => (
                <Badge key={a} variant="secondary">{formatAmenity(a)}</Badge>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Availability Check */}
      <div className="rounded-xl border bg-card p-6">
        <h2 className="text-lg font-semibold mb-4">Check Availability</h2>
        <div className="flex flex-col sm:flex-row gap-3">
          <div className="flex-1">
            <label className="text-sm text-muted-foreground mb-1 block">Check-in</label>
            <Input type="date" value={checkIn} onChange={(e) => setCheckIn(e.target.value)} />
          </div>
          <div className="flex-1">
            <label className="text-sm text-muted-foreground mb-1 block">Check-out</label>
            <Input type="date" value={checkOut} onChange={(e) => setCheckOut(e.target.value)} />
          </div>
        </div>
        {checkingAvail && <p className="text-sm text-muted-foreground mt-3">Checking availability...</p>}
        {availability && (
          <div className="mt-3 flex items-center gap-2">
            {availability.available ? (
              <>
                <CheckCircle className="h-5 w-5 text-green-600" />
                <span className="text-sm text-green-700">{availability.message || `${availability.availableRooms} rooms available`}</span>
              </>
            ) : (
              <>
                <XCircle className="h-5 w-5 text-red-500" />
                <span className="text-sm text-red-600">{availability.message || 'No rooms available for these dates'}</span>
              </>
            )}
          </div>
        )}
      </div>

      {/* Rooms */}
      {activeRooms.length > 0 && (
        <div>
          <h2 className="text-lg font-semibold mb-4">Rooms ({activeRooms.length})</h2>
          <div className="space-y-3">
            {activeRooms.map((room) => (
              <RoomCard key={room.id} room={room} />
            ))}
          </div>
        </div>
      )}

      {/* Reviews */}
      <ReviewSection entityType="HOTEL" entityId={hotelId} />
    </div>
  )
}

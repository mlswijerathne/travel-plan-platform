'use client'

import { use, useState } from 'react'
import { useHotelDetails, useUpdateHotel } from '@/hooks/use-hotels'
import { HotelForm } from '@/components/provider/HotelForm'
import { RoomManagement } from '@/components/provider/RoomManagement'
import { Button } from '@/components/ui/button'
import { ArrowLeft } from 'lucide-react'
import Link from 'next/link'

type Tab = 'details' | 'rooms'

export default function EditHotelPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params)
  const hotelId = Number(id)
  const [tab, setTab] = useState<Tab>('details')
  const { data, isLoading } = useHotelDetails(hotelId)
  const updateMutation = useUpdateHotel()
  const hotel = data?.data

  if (isLoading) {
    return (
      <div className="animate-pulse space-y-6">
        <div className="h-8 bg-muted rounded w-48" />
        <div className="h-96 bg-muted rounded-xl" />
      </div>
    )
  }

  if (!hotel) {
    return (
      <div className="text-center py-16">
        <p className="text-muted-foreground">Hotel not found</p>
        <Link href="/provider/hotels">
          <Button variant="outline" className="mt-4">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back
          </Button>
        </Link>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <Link href="/provider/hotels" className="inline-flex items-center text-sm text-muted-foreground hover:text-foreground">
        <ArrowLeft className="h-4 w-4 mr-1" />
        Back to Hotels
      </Link>

      <div>
        <h1 className="text-2xl font-bold">{hotel.name}</h1>
        <p className="text-muted-foreground">Manage hotel details and rooms</p>
      </div>

      <div className="flex gap-1.5">
        <Button
          variant={tab === 'details' ? 'default' : 'outline'}
          size="sm"
          onClick={() => setTab('details')}
        >
          Details
        </Button>
        <Button
          variant={tab === 'rooms' ? 'default' : 'outline'}
          size="sm"
          onClick={() => setTab('rooms')}
        >
          Rooms
        </Button>
      </div>

      <div className="rounded-xl border bg-card p-6">
        {tab === 'details' && (
          <HotelForm
            hotel={hotel}
            isPending={updateMutation.isPending}
            onSubmit={(data) => updateMutation.mutate({ id: hotelId, data })}
          />
        )}
        {tab === 'rooms' && (
          <RoomManagement hotelId={hotelId} />
        )}
      </div>
    </div>
  )
}

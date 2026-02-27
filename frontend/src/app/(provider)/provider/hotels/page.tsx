'use client'

import { useState } from 'react'
import { useMyHotels, useDeleteHotel } from '@/hooks/use-hotels'
import { Pagination } from '@/components/shared/Pagination'
import { EmptyState } from '@/components/shared/EmptyState'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Plus, Hotel, Star, Pencil, Trash2 } from 'lucide-react'
import Link from 'next/link'

export default function ProviderHotelsPage() {
  const [page, setPage] = useState(0)
  const { data, isLoading } = useMyHotels({ page, size: 10 })
  const deleteMutation = useDeleteHotel()

  const hotels = data?.data ?? []
  const pagination = data?.pagination

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">My Hotels</h1>
          <p className="text-muted-foreground">Manage your hotel properties</p>
        </div>
        <Link href="/provider/hotels/new">
          <Button>
            <Plus className="h-4 w-4 mr-2" />
            Add Hotel
          </Button>
        </Link>
      </div>

      {isLoading ? (
        <div className="space-y-3">
          {[1, 2, 3].map((i) => (
            <div key={i} className="rounded-xl border bg-card p-4 animate-pulse">
              <div className="h-5 bg-muted rounded w-48 mb-2" />
              <div className="h-4 bg-muted rounded w-32" />
            </div>
          ))}
        </div>
      ) : hotels.length === 0 ? (
        <EmptyState
          icon={Hotel}
          title="No hotels yet"
          description="Add your first hotel to start receiving bookings."
          action={
            <Link href="/provider/hotels/new">
              <Button><Plus className="h-4 w-4 mr-2" />Add Hotel</Button>
            </Link>
          }
        />
      ) : (
        <>
          <div className="space-y-3">
            {hotels.map((hotel) => (
              <div key={hotel.id} className="rounded-xl border bg-card p-4 flex items-center gap-4">
                <div className="h-12 w-12 rounded-lg bg-primary/10 flex items-center justify-center text-xl shrink-0">
                  🏨
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2">
                    <h3 className="font-medium truncate">{hotel.name}</h3>
                    {!hotel.isActive && <Badge variant="destructive" className="text-[10px]">Inactive</Badge>}
                  </div>
                  <div className="flex items-center gap-3 text-sm text-muted-foreground">
                    <span>{hotel.city}</span>
                    {hotel.reviewCount > 0 && (
                      <span className="flex items-center gap-1">
                        <Star className="h-3 w-3 fill-amber-400 text-amber-400" />
                        {hotel.averageRating.toFixed(1)} ({hotel.reviewCount})
                      </span>
                    )}
                    <span>{hotel.rooms?.length ?? 0} rooms</span>
                  </div>
                </div>
                <div className="flex gap-1 shrink-0">
                  <Link href={`/provider/hotels/${hotel.id}`}>
                    <Button variant="outline" size="sm">
                      <Pencil className="h-3.5 w-3.5 mr-1" />
                      Edit
                    </Button>
                  </Link>
                  <Button
                    variant="ghost"
                    size="sm"
                    className="text-destructive"
                    onClick={() => deleteMutation.mutate(hotel.id)}
                    disabled={deleteMutation.isPending}
                  >
                    <Trash2 className="h-3.5 w-3.5" />
                  </Button>
                </div>
              </div>
            ))}
          </div>
          {pagination && (
            <Pagination page={page} totalPages={pagination.totalPages} onPageChange={setPage} />
          )}
        </>
      )}
    </div>
  )
}

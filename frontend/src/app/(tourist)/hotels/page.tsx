'use client'

import { useState, useCallback } from 'react'
import { useHotels } from '@/hooks/use-hotels'
import { HotelCard } from '@/components/hotels/HotelCard'
import { HotelFilters } from '@/components/hotels/HotelFilters'
import { Pagination } from '@/components/shared/Pagination'
import { EmptyState } from '@/components/shared/EmptyState'
import { LoadingGrid } from '@/components/shared/LoadingGrid'
import { Hotel } from 'lucide-react'

export default function HotelsPage() {
  const [page, setPage] = useState(0)
  const [city, setCity] = useState('')
  const [minStarRating, setMinStarRating] = useState(0)
  const [searchQuery, setSearchQuery] = useState('')

  const { data, isLoading } = useHotels({
    city: city || undefined,
    minStarRating: minStarRating || undefined,
    page,
    size: 9,
  })

  const hotels = data?.data ?? []
  const pagination = data?.pagination

  const handleClear = useCallback(() => {
    setCity('')
    setMinStarRating(0)
    setSearchQuery('')
    setPage(0)
  }, [])

  return (
    <div className="space-y-8">
      {/* Page Header */}
      <div className="flex items-start gap-4">
        <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-xl bg-primary/10">
          <Hotel className="h-6 w-6 text-primary" />
        </div>
        <div>
          <h1 className="font-display text-3xl font-extrabold tracking-tight text-foreground">Hotels</h1>
          <p className="mt-1 text-lg text-muted-foreground">Find your perfect stay in Sri Lanka</p>
        </div>
      </div>

      {/* Filter Bar */}
      <div className="bg-card rounded-xl shadow-editorial border border-border/30 p-4">
        <HotelFilters
          city={city}
          minStarRating={minStarRating}
          searchQuery={searchQuery}
          onCityChange={(c) => { setCity(c); setPage(0) }}
          onStarRatingChange={(r) => { setMinStarRating(r); setPage(0) }}
          onSearchChange={setSearchQuery}
          onClear={handleClear}
        />
      </div>

      {isLoading ? (
        <LoadingGrid count={9} />
      ) : hotels.length === 0 ? (
        <EmptyState
          icon={Hotel}
          title="No hotels found"
          description="Try adjusting your filters to find more results."
        />
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {hotels.map((hotel) => (
              <div key={hotel.id} className="hover:-translate-y-1 hover:shadow-editorial-lg transition-all duration-300">
                <HotelCard hotel={hotel} />
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

'use client'

import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { Search, X } from 'lucide-react'
import { StarRating } from '@/components/shared/StarRating'

interface HotelFiltersProps {
  city: string
  minStarRating: number
  searchQuery: string
  onCityChange: (city: string) => void
  onStarRatingChange: (rating: number) => void
  onSearchChange: (query: string) => void
  onClear: () => void
}

const POPULAR_CITIES = ['Colombo', 'Kandy', 'Galle', 'Ella', 'Sigiriya', 'Nuwara Eliya', 'Trincomalee', 'Mirissa']

export function HotelFilters({
  city,
  minStarRating,
  searchQuery,
  onCityChange,
  onStarRatingChange,
  onSearchChange,
  onClear,
}: HotelFiltersProps) {
  const hasFilters = city || minStarRating > 0 || searchQuery

  return (
    <div className="space-y-4">
      <div className="relative">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <Input
          placeholder="Search hotels..."
          value={searchQuery}
          onChange={(e) => onSearchChange(e.target.value)}
          className="pl-10"
        />
      </div>

      <div className="flex flex-wrap items-center gap-3">
        <div className="flex flex-wrap gap-1.5">
          {POPULAR_CITIES.map((c) => (
            <Button
              key={c}
              variant={city === c ? 'default' : 'outline'}
              size="sm"
              onClick={() => onCityChange(city === c ? '' : c)}
              className="text-xs"
            >
              {c}
            </Button>
          ))}
        </div>
      </div>

      <div className="flex items-center gap-4">
        <span className="text-sm text-muted-foreground">Min Stars:</span>
        <StarRating
          rating={minStarRating}
          interactive
          onChange={(r) => onStarRatingChange(minStarRating === r ? 0 : r)}
          size="sm"
        />
        {hasFilters && (
          <Button variant="ghost" size="sm" onClick={onClear} className="ml-auto text-xs">
            <X className="h-3 w-3 mr-1" />
            Clear filters
          </Button>
        )}
      </div>
    </div>
  )
}

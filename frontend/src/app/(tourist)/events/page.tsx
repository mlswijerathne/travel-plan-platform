'use client'

import { useState, useCallback } from 'react'
import { useQuery } from '@tanstack/react-query'
import Link from 'next/link'
import { getPublicEvents } from '@/lib/api/events'
import type { EventSummary } from '@/types/event'
import { EVENT_CATEGORIES } from '@/types/event'
import { Card, CardContent } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/shared/EmptyState'
import { Pagination } from '@/components/shared/Pagination'
import { Ticket, MapPin, Calendar, Users, Search, X } from 'lucide-react'

function EventCard({ event }: { event: EventSummary }) {
  const start = new Date(event.startDateTime)
  const isAvailable = event.availableSeats > 0

  return (
    <Link href={`/events/${event.id}`}>
      <Card className="hover:shadow-lg transition-all duration-200 overflow-hidden h-full group">
        <div className="relative h-44 bg-gradient-to-br from-violet-100 to-teal-100 overflow-hidden">
          {event.coverImageUrl ? (
            <img src={event.coverImageUrl} alt={event.title} className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300" />
          ) : (
            <div className="w-full h-full flex items-center justify-center">
              <Ticket className="h-12 w-12 text-violet-300" />
            </div>
          )}
          <div className="absolute top-3 left-3 flex gap-1.5">
            <Badge className="bg-white/90 text-foreground text-xs font-medium backdrop-blur-sm">
              {event.category}
            </Badge>
            {event.authenticCultural && (
              <Badge className="bg-amber-500/90 text-white text-xs backdrop-blur-sm">Cultural</Badge>
            )}
          </div>
          {!isAvailable && (
            <div className="absolute inset-0 bg-black/40 flex items-center justify-center">
              <span className="bg-white text-foreground text-xs font-semibold px-3 py-1.5 rounded-full">Sold Out</span>
            </div>
          )}
        </div>
        <CardContent className="p-4 space-y-3">
          <h3 className="font-semibold text-sm leading-snug line-clamp-2 group-hover:text-primary transition-colors">
            {event.title}
          </h3>
          <div className="space-y-1.5 text-xs text-muted-foreground">
            <div className="flex items-center gap-1.5">
              <Calendar className="h-3 w-3 shrink-0" />
              <span>{start.toLocaleDateString('en-US', { day: 'numeric', month: 'short', year: 'numeric' })} · {start.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })}</span>
            </div>
            <div className="flex items-center gap-1.5">
              <MapPin className="h-3 w-3 shrink-0" />
              <span className="truncate">{event.venueName}, {event.location}</span>
            </div>
            <div className="flex items-center gap-1.5">
              <Users className="h-3 w-3 shrink-0" />
              <span>{isAvailable ? `${event.availableSeats} seats available` : 'Sold out'}</span>
            </div>
          </div>
          <div className="flex items-center justify-between pt-2 border-t border-border/50">
            <span className="font-bold text-foreground">
              {event.ticketPrice === 0 ? 'Free' : `Rs ${event.ticketPrice.toLocaleString()}`}
            </span>
            {event.vibe && (
              <Badge variant="outline" className="text-xs">{event.vibe}</Badge>
            )}
          </div>
        </CardContent>
      </Card>
    </Link>
  )
}

export default function EventsPage() {
  const [page, setPage] = useState(0)
  const [location, setLocation] = useState('')
  const [category, setCategory] = useState('')
  const [appliedFilters, setAppliedFilters] = useState({ location: '', category: '' })

  const { data, isLoading } = useQuery({
    queryKey: ['public-events', appliedFilters, page],
    queryFn: () => getPublicEvents({
      location: appliedFilters.location || undefined,
      category: appliedFilters.category || undefined,
      page,
      size: 12,
    }),
  })

  const events = data?.data ?? []
  const pagination = data?.pagination

  const handleSearch = useCallback(() => {
    setAppliedFilters({ location, category })
    setPage(0)
  }, [location, category])

  const handleClear = useCallback(() => {
    setLocation('')
    setCategory('')
    setAppliedFilters({ location: '', category: '' })
    setPage(0)
  }, [])

  const hasFilters = appliedFilters.location || appliedFilters.category

  return (
    <div className="space-y-6">
      <div>
        <h1 className="font-display text-2xl font-bold">Events</h1>
        <p className="text-muted-foreground">Discover cultural events, festivals, and local experiences in Sri Lanka</p>
      </div>

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Search by city or location..."
            value={location}
            onChange={e => setLocation(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && handleSearch()}
            className="pl-9 h-10"
          />
        </div>
        <Select value={category} onValueChange={setCategory}>
          <SelectTrigger className="w-full sm:w-44 h-10">
            <SelectValue placeholder="All categories" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="">All categories</SelectItem>
            {EVENT_CATEGORIES.map(c => (
              <SelectItem key={c} value={c}>{c.charAt(0) + c.slice(1).toLowerCase()}</SelectItem>
            ))}
          </SelectContent>
        </Select>
        <Button onClick={handleSearch} className="h-10 gap-2 shrink-0">
          <Search className="h-4 w-4" />
          Search
        </Button>
        {hasFilters && (
          <Button variant="ghost" onClick={handleClear} className="h-10 gap-1.5 shrink-0 text-muted-foreground">
            <X className="h-4 w-4" />
            Clear
          </Button>
        )}
      </div>

      {isLoading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-5">
          {Array.from({ length: 8 }).map((_, i) => (
            <div key={i} className="space-y-3">
              <Skeleton className="h-44 rounded-xl" />
              <Skeleton className="h-4 w-3/4" />
              <Skeleton className="h-3 w-1/2" />
            </div>
          ))}
        </div>
      ) : events.length === 0 ? (
        <EmptyState
          icon={Ticket}
          title="No events found"
          description={hasFilters ? 'Try clearing your filters to see all events.' : 'No events available right now. Check back soon!'}
        />
      ) : (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-5">
            {events.map(event => <EventCard key={event.id} event={event} />)}
          </div>
          {pagination && (
            <Pagination page={page} totalPages={pagination.totalPages} onPageChange={setPage} />
          )}
        </>
      )}
    </div>
  )
}

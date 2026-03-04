'use client'

import { useEffect, useState } from 'react'
import { useParams, useRouter } from 'next/navigation'
import { getPublicEvent, registerForEvent, checkEventAvailability } from '@/lib/api/events'
import type { EventResponse, EventAvailability } from '@/types/event'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Separator } from '@/components/ui/separator'
import {
  Ticket,
  MapPin,
  Calendar,
  Users,
  ArrowLeft,
  Clock,
  Info,
  CheckCircle2,
  XCircle,
} from 'lucide-react'
import { toast } from 'sonner'
import Link from 'next/link'

export default function EventDetailPage() {
  const { id } = useParams<{ id: string }>()
  const router = useRouter()
  const [event, setEvent] = useState<EventResponse | null>(null)
  const [availability, setAvailability] = useState<EventAvailability | null>(null)
  const [loading, setLoading] = useState(true)
  const [bookingOpen, setBookingOpen] = useState(false)
  const [quantity, setQuantity] = useState(1)
  const [booking, setBooking] = useState(false)

  useEffect(() => {
    async function load() {
      try {
        const [evtRes, availRes] = await Promise.allSettled([
          getPublicEvent(Number(id)),
          checkEventAvailability(Number(id)),
        ])
        if (evtRes.status === 'fulfilled') setEvent(evtRes.value)
        if (availRes.status === 'fulfilled') setAvailability(availRes.value.data)
      } catch {
        toast.error('Failed to load event')
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [id])

  async function handleBooking() {
    setBooking(true)
    try {
      await registerForEvent(Number(id), { quantity })
      toast.success(`${quantity} ticket${quantity > 1 ? 's' : ''} booked successfully!`)
      setBookingOpen(false)
      // Refresh availability
      const res = await checkEventAvailability(Number(id))
      setAvailability(res.data)
    } catch {
      toast.error('Failed to book tickets. Please try again.')
    } finally {
      setBooking(false)
    }
  }

  if (loading) {
    return (
      <div className="space-y-6">
        <Skeleton className="h-64 w-full rounded-xl" />
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="md:col-span-2 space-y-4">
            <Skeleton className="h-8 w-3/4" />
            <Skeleton className="h-4 w-full" />
            <Skeleton className="h-4 w-2/3" />
          </div>
          <Skeleton className="h-48 rounded-xl" />
        </div>
      </div>
    )
  }

  if (!event) {
    return (
      <div className="text-center py-16">
        <Ticket className="h-12 w-12 text-muted-foreground/30 mx-auto mb-4" />
        <p className="text-muted-foreground font-medium">Event not found</p>
        <Link href="/events"><Button variant="outline" className="mt-4">Browse Events</Button></Link>
      </div>
    )
  }

  const start = new Date(event.startDateTime)
  const end = new Date(event.endDateTime)
  const isAvailable = availability ? availability.isAvailable : event.availableSeats > 0
  const totalCost = event.ticketPrice * quantity

  return (
    <div className="space-y-6">
      <Link href="/events">
        <Button variant="ghost" size="sm" className="gap-2 -ml-2">
          <ArrowLeft className="h-4 w-4" />
          Back to Events
        </Button>
      </Link>

      {/* Hero Image */}
      <div className="relative h-64 md:h-80 rounded-2xl overflow-hidden bg-gradient-to-br from-violet-100 to-teal-100">
        {event.coverImageUrl ? (
          <img src={event.coverImageUrl} alt={event.title} className="w-full h-full object-cover" />
        ) : (
          <div className="w-full h-full flex items-center justify-center">
            <Ticket className="h-20 w-20 text-violet-200" />
          </div>
        )}
        <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent" />
        <div className="absolute bottom-4 left-5 flex gap-2">
          <Badge className="bg-white/20 backdrop-blur-sm text-white border-white/30">{event.category}</Badge>
          {event.authenticCultural && (
            <Badge className="bg-amber-500/80 backdrop-blur-sm text-white border-amber-400/30">Authentic Cultural</Badge>
          )}
          {event.vibe && (
            <Badge className="bg-white/20 backdrop-blur-sm text-white border-white/30">{event.vibe}</Badge>
          )}
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Main Info */}
        <div className="md:col-span-2 space-y-6">
          <div>
            <h1 className="font-display text-3xl font-bold text-foreground">{event.title}</h1>
            {event.description && (
              <p className="text-muted-foreground mt-3 leading-relaxed">{event.description}</p>
            )}
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="flex items-start gap-3 p-3 bg-muted/40 rounded-xl">
              <Calendar className="h-5 w-5 text-primary mt-0.5 shrink-0" />
              <div>
                <p className="text-xs text-muted-foreground font-medium">Date & Time</p>
                <p className="text-sm font-semibold">{start.toLocaleDateString('en-US', { day: 'numeric', month: 'long', year: 'numeric' })}</p>
                <p className="text-xs text-muted-foreground">
                  {start.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })} –{' '}
                  {end.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })}
                </p>
              </div>
            </div>
            <div className="flex items-start gap-3 p-3 bg-muted/40 rounded-xl">
              <MapPin className="h-5 w-5 text-primary mt-0.5 shrink-0" />
              <div>
                <p className="text-xs text-muted-foreground font-medium">Location</p>
                <p className="text-sm font-semibold">{event.venueName}</p>
                <p className="text-xs text-muted-foreground">{event.location}</p>
              </div>
            </div>
            <div className="flex items-start gap-3 p-3 bg-muted/40 rounded-xl">
              <Users className="h-5 w-5 text-primary mt-0.5 shrink-0" />
              <div>
                <p className="text-xs text-muted-foreground font-medium">Capacity</p>
                <p className="text-sm font-semibold">{availability?.availableSeats ?? event.availableSeats} seats left</p>
                <p className="text-xs text-muted-foreground">of {availability?.totalCapacity ?? event.maxCapacity} total</p>
              </div>
            </div>
            <div className="flex items-start gap-3 p-3 bg-muted/40 rounded-xl">
              <Ticket className="h-5 w-5 text-primary mt-0.5 shrink-0" />
              <div>
                <p className="text-xs text-muted-foreground font-medium">Ticket Price</p>
                <p className="text-sm font-semibold">
                  {!event.ticketPrice ? 'Free Entry' : `Rs ${event.ticketPrice.toLocaleString()}`}
                </p>
                <p className="text-xs text-muted-foreground">per person</p>
              </div>
            </div>
          </div>

          {event.requirements && (
            <div className="flex items-start gap-3 p-4 bg-amber-50 border border-amber-200 rounded-xl">
              <Info className="h-5 w-5 text-amber-600 mt-0.5 shrink-0" />
              <div>
                <p className="text-sm font-semibold text-amber-800">What to bring / Requirements</p>
                <p className="text-sm text-amber-700 mt-1">{event.requirements}</p>
              </div>
            </div>
          )}

          {event.cancellationPolicy && (
            <div>
              <h3 className="font-semibold text-sm mb-2">Cancellation Policy</h3>
              <p className="text-sm text-muted-foreground">{event.cancellationPolicy}</p>
            </div>
          )}
        </div>

        {/* Booking Card */}
        <div className="space-y-4">
          <Card className="shadow-sm sticky top-20">
            <CardHeader className="pb-3">
              <CardTitle className="text-base">Book Tickets</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between py-2">
                <span className="text-sm text-muted-foreground">Price per ticket</span>
                <span className="font-semibold">
                  {event.ticketPrice === 0 ? 'Free' : `Rs ${event.ticketPrice.toLocaleString()}`}
                </span>
              </div>
              <Separator />
              <div className="flex items-center gap-3">
                {isAvailable ? (
                  <div className="flex items-center gap-1.5 text-emerald-600 text-xs font-medium">
                    <CheckCircle2 className="h-4 w-4" />
                    {availability?.availableSeats ?? event.availableSeats} seats available
                  </div>
                ) : (
                  <div className="flex items-center gap-1.5 text-destructive text-xs font-medium">
                    <XCircle className="h-4 w-4" />
                    Sold out
                  </div>
                )}
              </div>
              <Button
                className="w-full h-11"
                disabled={!isAvailable}
                onClick={() => setBookingOpen(true)}
              >
                {isAvailable ? 'Book Tickets' : 'Sold Out'}
              </Button>
              {isAvailable && (
                <p className="text-xs text-center text-muted-foreground">
                  You&apos;ll receive a ticket reference after booking
                </p>
              )}
            </CardContent>
          </Card>
        </div>
      </div>

      {/* Booking Dialog */}
      <Dialog open={bookingOpen} onOpenChange={setBookingOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Book Tickets</DialogTitle>
          </DialogHeader>
          <div className="space-y-5 py-2">
            <div className="space-y-2">
              <Label>Number of Tickets</Label>
              <Input
                type="number"
                min={1}
                max={Math.min(10, availability?.availableSeats ?? event.availableSeats)}
                value={quantity}
                onChange={e => setQuantity(Math.max(1, +e.target.value))}
                className="h-11"
              />
              <p className="text-xs text-muted-foreground">
                Maximum 10 tickets per booking. {availability?.availableSeats ?? event.availableSeats} seats remaining.
              </p>
            </div>
            <Separator />
            <div className="flex items-center justify-between font-semibold">
              <span>Total</span>
              <span>{event.ticketPrice === 0 ? 'Free' : `Rs ${totalCost.toLocaleString()}`}</span>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setBookingOpen(false)}>Cancel</Button>
            <Button onClick={handleBooking} disabled={booking}>
              {booking ? 'Booking...' : `Confirm ${quantity} ticket${quantity > 1 ? 's' : ''}`}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}

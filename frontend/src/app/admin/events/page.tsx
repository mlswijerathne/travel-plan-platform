'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'
import { getMyAdminEvents, deleteEvent } from '@/lib/api/events'
import type { EventSummary, EventStatus } from '@/types/event'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent } from '@/components/ui/card'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Skeleton } from '@/components/ui/skeleton'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from '@/components/ui/alert-dialog'
import { Plus, Ticket, Pencil, Trash2, MapPin, Calendar, Users } from 'lucide-react'
import { toast } from 'sonner'

const STATUS_COLORS: Record<EventStatus, string> = {
  PUBLISHED: 'border-emerald-300 bg-emerald-50 text-emerald-700',
  DRAFT: 'border-gray-200 bg-gray-50 text-gray-500',
  CANCELLED: 'border-red-200 bg-red-50 text-red-600',
  COMPLETED: 'border-blue-200 bg-blue-50 text-blue-600',
}

export default function AdminEventsPage() {
  const [events, setEvents] = useState<EventSummary[]>([])
  const [loading, setLoading] = useState(true)
  const [statusFilter, setStatusFilter] = useState<EventStatus | 'ALL'>('ALL')

  useEffect(() => { load() }, [statusFilter])

  async function load() {
    setLoading(true)
    try {
      const res = await getMyAdminEvents({
        status: statusFilter !== 'ALL' ? statusFilter : undefined,
        page: 0,
        size: 50,
      })
      setEvents(res.data ?? [])
    } catch {
      toast.error('Failed to load events')
    } finally {
      setLoading(false)
    }
  }

  async function handleDelete(id: number, title: string) {
    try {
      await deleteEvent(id)
      toast.success(`"${title}" deleted`)
      setEvents(prev => prev.filter(e => e.id !== id))
    } catch {
      toast.error('Failed to delete event')
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="font-display text-2xl font-bold">Events</h1>
          <p className="text-muted-foreground text-sm mt-1">{events.length} events</p>
        </div>
        <Link href="/admin/events/new">
          <Button className="gap-2"><Plus className="h-4 w-4" />New Event</Button>
        </Link>
      </div>

      {/* Filter */}
      <div className="flex items-center gap-3">
        <Select value={statusFilter} onValueChange={v => setStatusFilter(v as EventStatus | 'ALL')}>
          <SelectTrigger className="w-40 h-9">
            <SelectValue placeholder="All statuses" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">All statuses</SelectItem>
            <SelectItem value="PUBLISHED">Published</SelectItem>
            <SelectItem value="DRAFT">Draft</SelectItem>
            <SelectItem value="COMPLETED">Completed</SelectItem>
            <SelectItem value="CANCELLED">Cancelled</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {loading ? (
        <div className="space-y-3">
          {[1, 2, 3].map(i => <Skeleton key={i} className="h-28 rounded-xl" />)}
        </div>
      ) : events.length === 0 ? (
        <Card className="border-dashed">
          <CardContent className="py-16 text-center">
            <Ticket className="h-12 w-12 text-muted-foreground/30 mx-auto mb-4" />
            <p className="font-medium text-muted-foreground mb-3">No events found</p>
            <Link href="/admin/events/new"><Button>Create First Event</Button></Link>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-3">
          {events.map(evt => (
            <Card key={evt.id} className="shadow-sm hover:shadow-md transition-shadow">
              <CardContent className="flex items-center gap-4 py-4">
                {evt.coverImageUrl ? (
                  <img src={evt.coverImageUrl} alt={evt.title} className="h-16 w-24 rounded-lg object-cover shrink-0" />
                ) : (
                  <div className="h-16 w-24 rounded-lg bg-muted flex items-center justify-center shrink-0">
                    <Ticket className="h-6 w-6 text-muted-foreground/50" />
                  </div>
                )}
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 mb-1">
                    <h3 className="font-semibold text-sm truncate">{evt.title}</h3>
                    <Badge variant="outline" className={`text-xs shrink-0 ${STATUS_COLORS[evt.status]}`}>
                      {evt.status}
                    </Badge>
                  </div>
                  <div className="flex items-center gap-4 text-xs text-muted-foreground">
                    <span className="flex items-center gap-1"><MapPin className="h-3 w-3" /> {evt.location}</span>
                    <span className="flex items-center gap-1">
                      <Calendar className="h-3 w-3" />
                      {new Date(evt.startDateTime).toLocaleDateString()}
                    </span>
                    <span className="flex items-center gap-1"><Users className="h-3 w-3" /> {evt.availableSeats} seats left</span>
                  </div>
                  <p className="text-sm font-medium mt-1">Rs {evt.ticketPrice.toLocaleString()}</p>
                </div>
                <div className="flex items-center gap-2 shrink-0">
                  <Link href={`/admin/events/${evt.id}/edit`}>
                    <Button variant="outline" size="sm" className="gap-1.5"><Pencil className="h-3.5 w-3.5" />Edit</Button>
                  </Link>
                  <AlertDialog>
                    <AlertDialogTrigger asChild>
                      <Button variant="outline" size="sm" className="gap-1.5 text-destructive border-destructive/20 hover:bg-destructive/5 hover:text-destructive">
                        <Trash2 className="h-3.5 w-3.5" />Delete
                      </Button>
                    </AlertDialogTrigger>
                    <AlertDialogContent>
                      <AlertDialogHeader>
                        <AlertDialogTitle>Delete event?</AlertDialogTitle>
                        <AlertDialogDescription>
                          This will permanently delete &quot;{evt.title}&quot;. This cannot be undone.
                        </AlertDialogDescription>
                      </AlertDialogHeader>
                      <AlertDialogFooter>
                        <AlertDialogCancel>Cancel</AlertDialogCancel>
                        <AlertDialogAction onClick={() => handleDelete(evt.id, evt.title)} className="bg-destructive hover:bg-destructive/90">
                          Delete
                        </AlertDialogAction>
                      </AlertDialogFooter>
                    </AlertDialogContent>
                  </AlertDialog>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}

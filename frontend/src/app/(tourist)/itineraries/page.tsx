'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'
import { getItineraries, createItinerary } from '@/lib/api/itinerary'
import type { ItineraryDTO, CreateItineraryRequest } from '@/types/itinerary'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Card, CardContent } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog'
import { Map, Plus, Calendar, ChevronRight, Clock } from 'lucide-react'
import { toast } from 'sonner'

const STATUS_COLORS: Record<string, string> = {
  PLANNING: 'bg-blue-50 text-blue-600 border-blue-200',
  ACTIVE: 'bg-emerald-50 text-emerald-600 border-emerald-200',
  COMPLETED: 'bg-gray-50 text-gray-500 border-gray-200',
  CANCELLED: 'bg-red-50 text-red-500 border-red-200',
}

export default function ItinerariesPage() {
  const [itineraries, setItineraries] = useState<ItineraryDTO[]>([])
  const [loading, setLoading] = useState(true)
  const [createOpen, setCreateOpen] = useState(false)
  const [creating, setCreating] = useState(false)

  // Create form
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')

  useEffect(() => { load() }, [])

  async function load() {
    try {
      const data = await getItineraries()
      setItineraries(data)
    } catch {
      toast.error('Failed to load itineraries')
    } finally {
      setLoading(false)
    }
  }

  function resetForm() {
    setTitle('')
    setDescription('')
    setStartDate('')
    setEndDate('')
  }

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault()
    if (!title || !startDate || !endDate) {
      toast.error('Title, start date and end date are required')
      return
    }
    if (new Date(endDate) < new Date(startDate)) {
      toast.error('End date must be after start date')
      return
    }
    setCreating(true)
    try {
      const body: CreateItineraryRequest = { title, description: description || undefined, startDate, endDate }
      const created = await createItinerary(body)
      toast.success('Itinerary created!')
      setCreateOpen(false)
      resetForm()
      setItineraries(prev => [created, ...prev])
    } catch {
      toast.error('Failed to create itinerary')
    } finally {
      setCreating(false)
    }
  }

  function getDays(start: string, end: string) {
    const diff = new Date(end).getTime() - new Date(start).getTime()
    return Math.ceil(diff / (1000 * 60 * 60 * 24)) + 1
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="font-display text-2xl font-bold">My Itineraries</h1>
          <p className="text-muted-foreground">Plan and manage your Sri Lanka trips</p>
        </div>
        <Button onClick={() => setCreateOpen(true)} className="gap-2">
          <Plus className="h-4 w-4" />
          New Itinerary
        </Button>
      </div>

      {loading ? (
        <div className="space-y-3">
          {[1, 2, 3].map(i => <Skeleton key={i} className="h-28 rounded-xl" />)}
        </div>
      ) : itineraries.length === 0 ? (
        <Card className="border-dashed">
          <CardContent className="py-16 text-center">
            <Map className="h-12 w-12 text-muted-foreground/30 mx-auto mb-4" />
            <p className="font-medium text-muted-foreground mb-3">No itineraries yet</p>
            <p className="text-sm text-muted-foreground mb-4">Start planning your perfect Sri Lanka trip</p>
            <Button onClick={() => setCreateOpen(true)} className="gap-2">
              <Plus className="h-4 w-4" />
              Create Itinerary
            </Button>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-3">
          {itineraries.map(itin => (
            <Link key={itin.id} href={`/itineraries/${itin.id}`}>
              <Card className="hover:shadow-md transition-shadow cursor-pointer">
                <CardContent className="flex items-center gap-4 py-4">
                  <div className="h-12 w-12 rounded-xl bg-primary/10 flex items-center justify-center shrink-0">
                    <Map className="h-6 w-6 text-primary" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1">
                      <h3 className="font-semibold text-sm truncate">{itin.title}</h3>
                      <Badge variant="outline" className={`text-xs shrink-0 ${STATUS_COLORS[itin.status] ?? ''}`}>
                        {itin.status}
                      </Badge>
                    </div>
                    <div className="flex items-center gap-4 text-xs text-muted-foreground">
                      <span className="flex items-center gap-1">
                        <Calendar className="h-3 w-3" />
                        {new Date(itin.startDate).toLocaleDateString()} – {new Date(itin.endDate).toLocaleDateString()}
                      </span>
                      <span className="flex items-center gap-1">
                        <Clock className="h-3 w-3" />
                        {getDays(itin.startDate, itin.endDate)} days
                      </span>
                    </div>
                    {itin.description && (
                      <p className="text-xs text-muted-foreground mt-1 truncate">{itin.description}</p>
                    )}
                  </div>
                  <ChevronRight className="h-4 w-4 text-muted-foreground shrink-0" />
                </CardContent>
              </Card>
            </Link>
          ))}
        </div>
      )}

      {/* Create Dialog */}
      <Dialog open={createOpen} onOpenChange={v => { setCreateOpen(v); if (!v) resetForm() }}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Create Itinerary</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleCreate}>
            <div className="space-y-4 py-2">
              <div className="space-y-2">
                <Label>Trip Name *</Label>
                <Input value={title} onChange={e => setTitle(e.target.value)} placeholder="e.g. Cultural Triangle Tour" className="h-11" required />
              </div>
              <div className="space-y-2">
                <Label>Description</Label>
                <Textarea value={description} onChange={e => setDescription(e.target.value)} placeholder="Brief description of your trip..." rows={2} />
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-2">
                  <Label>Start Date *</Label>
                  <Input type="date" value={startDate} onChange={e => setStartDate(e.target.value)} className="h-11" required />
                </div>
                <div className="space-y-2">
                  <Label>End Date *</Label>
                  <Input type="date" value={endDate} onChange={e => setEndDate(e.target.value)} className="h-11" required />
                </div>
              </div>
            </div>
            <DialogFooter className="mt-4">
              <Button type="button" variant="outline" onClick={() => setCreateOpen(false)}>Cancel</Button>
              <Button type="submit" disabled={creating}>{creating ? 'Creating...' : 'Create'}</Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  )
}

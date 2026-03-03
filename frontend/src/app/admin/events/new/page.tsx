'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { createEvent } from '@/lib/api/events'
import type { CreateEventRequest } from '@/types/event'
import { EVENT_CATEGORIES, EVENT_VIBES } from '@/types/event'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Switch } from '@/components/ui/switch'
import { ArrowLeft } from 'lucide-react'
import { toast } from 'sonner'
import Link from 'next/link'

export default function NewEventPage() {
  const router = useRouter()
  const [saving, setSaving] = useState(false)

  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [category, setCategory] = useState('')
  const [location, setLocation] = useState('')
  const [venueName, setVenueName] = useState('')
  const [startDateTime, setStartDateTime] = useState('')
  const [endDateTime, setEndDateTime] = useState('')
  const [ticketPrice, setTicketPrice] = useState(0)
  const [maxCapacity, setMaxCapacity] = useState(100)
  const [vibe, setVibe] = useState('')
  const [authenticCultural, setAuthenticCultural] = useState(false)
  const [coverImageUrl, setCoverImageUrl] = useState('')
  const [requirements, setRequirements] = useState('')
  const [cancellationPolicy, setCancellationPolicy] = useState('')

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!title || !category || !location || !venueName || !startDateTime || !endDateTime) {
      toast.error('Please fill all required fields')
      return
    }
    setSaving(true)
    try {
      const body: CreateEventRequest = {
        title, description, category, location, venueName,
        startDateTime, endDateTime, ticketPrice, maxCapacity,
        vibe: vibe || undefined,
        authenticCultural,
        coverImageUrl: coverImageUrl || undefined,
        requirements: requirements || undefined,
        cancellationPolicy: cancellationPolicy || undefined,
      }
      await createEvent(body)
      toast.success('Event created successfully')
      router.push('/admin/events')
    } catch {
      toast.error('Failed to create event')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="space-y-6 max-w-3xl">
      <div className="flex items-center gap-3">
        <Link href="/admin/events">
          <Button variant="ghost" size="sm" className="gap-2"><ArrowLeft className="h-4 w-4" />Back</Button>
        </Link>
        <div>
          <h1 className="font-display text-2xl font-bold">New Event</h1>
          <p className="text-muted-foreground text-sm">Create a new event for tourists to discover and attend</p>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        <Card className="shadow-sm">
          <CardHeader><CardTitle className="text-base">Event Details</CardTitle></CardHeader>
          <CardContent className="space-y-5">
            <div className="space-y-2">
              <Label>Title *</Label>
              <Input value={title} onChange={e => setTitle(e.target.value)} placeholder="e.g. Kandy Esala Perahera" className="h-11" required />
            </div>
            <div className="space-y-2">
              <Label>Description</Label>
              <Textarea value={description} onChange={e => setDescription(e.target.value)} placeholder="Describe this event..." rows={3} />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>Category *</Label>
                <Select value={category} onValueChange={setCategory} required>
                  <SelectTrigger className="h-11"><SelectValue placeholder="Select category" /></SelectTrigger>
                  <SelectContent>
                    {EVENT_CATEGORIES.map(c => (
                      <SelectItem key={c} value={c}>{c.charAt(0) + c.slice(1).toLowerCase()}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label>Vibe</Label>
                <Select value={vibe} onValueChange={setVibe}>
                  <SelectTrigger className="h-11"><SelectValue placeholder="Select vibe" /></SelectTrigger>
                  <SelectContent>
                    {EVENT_VIBES.map(v => (
                      <SelectItem key={v} value={v}>{v.charAt(0) + v.slice(1).toLowerCase()}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="shadow-sm">
          <CardHeader><CardTitle className="text-base">Location & Schedule</CardTitle></CardHeader>
          <CardContent className="space-y-5">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>Location / City *</Label>
                <Input value={location} onChange={e => setLocation(e.target.value)} placeholder="e.g. Kandy" className="h-11" required />
              </div>
              <div className="space-y-2">
                <Label>Venue Name *</Label>
                <Input value={venueName} onChange={e => setVenueName(e.target.value)} placeholder="e.g. Temple of the Tooth" className="h-11" required />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>Start Date & Time *</Label>
                <Input type="datetime-local" value={startDateTime} onChange={e => setStartDateTime(e.target.value)} className="h-11" required />
              </div>
              <div className="space-y-2">
                <Label>End Date & Time *</Label>
                <Input type="datetime-local" value={endDateTime} onChange={e => setEndDateTime(e.target.value)} className="h-11" required />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="shadow-sm">
          <CardHeader><CardTitle className="text-base">Capacity & Pricing</CardTitle></CardHeader>
          <CardContent className="space-y-5">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>Ticket Price (Rs.) *</Label>
                <Input type="number" min={0} value={ticketPrice} onChange={e => setTicketPrice(+e.target.value)} className="h-11" required />
              </div>
              <div className="space-y-2">
                <Label>Max Capacity *</Label>
                <Input type="number" min={1} value={maxCapacity} onChange={e => setMaxCapacity(+e.target.value)} className="h-11" required />
              </div>
            </div>
            <div className="space-y-2">
              <Label>Cover Image URL</Label>
              <Input value={coverImageUrl} onChange={e => setCoverImageUrl(e.target.value)} placeholder="https://..." className="h-11" />
            </div>
            <div className="flex items-center gap-3">
              <Switch checked={authenticCultural} onCheckedChange={setAuthenticCultural} id="cultural" />
              <Label htmlFor="cultural">Authentic Cultural Experience</Label>
            </div>
          </CardContent>
        </Card>

        <Card className="shadow-sm">
          <CardHeader><CardTitle className="text-base">Additional Info</CardTitle></CardHeader>
          <CardContent className="space-y-5">
            <div className="space-y-2">
              <Label>Requirements / What to Bring</Label>
              <Textarea value={requirements} onChange={e => setRequirements(e.target.value)} rows={2} placeholder="e.g. Dress modestly, bring comfortable shoes" />
            </div>
            <div className="space-y-2">
              <Label>Cancellation Policy</Label>
              <Textarea value={cancellationPolicy} onChange={e => setCancellationPolicy(e.target.value)} rows={2} placeholder="e.g. Full refund if cancelled 48 hours before" />
            </div>
          </CardContent>
        </Card>

        <div className="flex justify-end gap-3">
          <Link href="/admin/events"><Button type="button" variant="outline">Cancel</Button></Link>
          <Button type="submit" disabled={saving}>{saving ? 'Creating...' : 'Create Event'}</Button>
        </div>
      </form>
    </div>
  )
}

'use client'

import { useState } from 'react'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import type { TripPlanFormData } from '@/types/trip-plan'
import { cn } from '@/lib/utils'

interface TripPlanDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  onSubmit: (data: TripPlanFormData) => void
  sessionId: string | null
  disabled?: boolean
}

const INTEREST_OPTIONS = [
  'Beaches',
  'Culture',
  'Wildlife',
  'Adventure',
  'Food',
  'History',
  'Nature',
  'Nightlife',
  'Shopping',
  'Wellness',
]

const TRAVEL_STYLES = [
  { value: 'budget', label: 'Budget' },
  { value: 'comfort', label: 'Comfort' },
  { value: 'luxury', label: 'Luxury' },
  { value: 'adventure', label: 'Adventure' },
]

export function TripPlanDialog({ open, onOpenChange, onSubmit, sessionId, disabled }: TripPlanDialogProps) {
  const [destination, setDestination] = useState('')
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const [budget, setBudget] = useState('')
  const [travelers, setTravelers] = useState('2')
  const [travelStyle, setTravelStyle] = useState('comfort')
  const [selectedInterests, setSelectedInterests] = useState<string[]>([])

  function toggleInterest(interest: string) {
    setSelectedInterests(prev =>
      prev.includes(interest)
        ? prev.filter(i => i !== interest)
        : [...prev, interest]
    )
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!destination.trim() || !startDate || !endDate) return

    const data: TripPlanFormData = {
      destination: destination.trim(),
      startDate,
      endDate,
      sessionId: sessionId ?? undefined,
    }
    if (budget) data.budget = Number(budget)
    if (travelers) data.travelers = Number(travelers)
    if (travelStyle) data.travelStyle = travelStyle
    if (selectedInterests.length > 0) data.interests = selectedInterests

    onSubmit(data)
    onOpenChange(false)
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Plan Your Trip</DialogTitle>
          <DialogDescription>
            Fill in your trip details and our AI will create a personalized itinerary.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="destination">Destination *</Label>
            <Input
              id="destination"
              placeholder="e.g. Colombo, Kandy, Galle..."
              value={destination}
              onChange={(e) => setDestination(e.target.value)}
              required
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-2">
              <Label htmlFor="startDate">Start Date *</Label>
              <Input
                id="startDate"
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="endDate">End Date *</Label>
              <Input
                id="endDate"
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                min={startDate}
                required
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-2">
              <Label htmlFor="budget">Budget (LKR)</Label>
              <Input
                id="budget"
                type="number"
                placeholder="e.g. 150000"
                value={budget}
                onChange={(e) => setBudget(e.target.value)}
                min="0"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="travelers">Travelers</Label>
              <Input
                id="travelers"
                type="number"
                value={travelers}
                onChange={(e) => setTravelers(e.target.value)}
                min="1"
                max="20"
              />
            </div>
          </div>

          <div className="space-y-2">
            <Label>Travel Style</Label>
            <Select value={travelStyle} onValueChange={setTravelStyle}>
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {TRAVEL_STYLES.map((style) => (
                  <SelectItem key={style.value} value={style.value}>
                    {style.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label>Interests</Label>
            <div className="flex flex-wrap gap-2">
              {INTEREST_OPTIONS.map((interest) => (
                <button
                  key={interest}
                  type="button"
                  onClick={() => toggleInterest(interest)}
                  className={cn(
                    'rounded-full px-3 py-1 text-xs border transition-colors',
                    selectedInterests.includes(interest)
                      ? 'bg-primary text-primary-foreground border-primary'
                      : 'bg-background text-foreground border-input hover:bg-muted'
                  )}
                >
                  {interest}
                </button>
              ))}
            </div>
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Cancel
            </Button>
            <Button type="submit" disabled={!destination.trim() || !startDate || !endDate || disabled}>
              Generate Plan
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

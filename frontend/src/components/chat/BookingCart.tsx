'use client'

import { useState } from 'react'
import type { ProviderResult } from '@/types/chat'
import type { ProviderType } from '@/types/booking'
import { createBooking } from '@/lib/api/booking'
import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetFooter } from '@/components/ui/sheet'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Badge } from '@/components/ui/badge'
import { formatCurrency } from '@/lib/utils'
import { toast } from 'sonner'
import { Trash2, ShoppingCart, Building2, User, Car, CalendarDays, Loader2 } from 'lucide-react'
import { useRouter } from 'next/navigation'

const TYPE_ICONS: Record<string, typeof Building2> = {
  HOTEL: Building2,
  TOUR_GUIDE: User,
  VEHICLE: Car,
  EVENT: CalendarDays,
}

const TYPE_LABELS: Record<string, string> = {
  HOTEL: 'Hotel',
  TOUR_GUIDE: 'Tour Guide',
  VEHICLE: 'Vehicle',
  EVENT: 'Event',
}

interface BookingCartProps {
  items: ProviderResult[]
  open: boolean
  onOpenChange: (open: boolean) => void
  onRemoveItem: (providerId: string) => void
  onClearAll: () => void
}

export function BookingCart({ items, open, onOpenChange, onRemoveItem, onClearAll }: BookingCartProps) {
  const router = useRouter()
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const [notes, setNotes] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const totalAmount = items.reduce((sum, item) => sum + (item.price || 0), 0)

  async function handleSubmitBooking() {
    if (!startDate || !endDate) {
      toast.error('Please select start and end dates')
      return
    }

    if (new Date(endDate) <= new Date(startDate)) {
      toast.error('End date must be after start date')
      return
    }

    if (items.length === 0) {
      toast.error('Add at least one provider to your booking')
      return
    }

    setIsSubmitting(true)
    try {
      const response = await createBooking({
        startDate,
        endDate,
        notes: notes || undefined,
        items: items.map(item => ({
          providerType: mapProviderType(item.type),
          providerId: Number(item.id),
          itemName: item.name,
          quantity: 1,
          unitPrice: item.price || 0,
          startDate,
          endDate,
        })),
      })

      toast.success(`Booking created! Reference: ${response.data.bookingReference}`)
      onClearAll()
      onOpenChange(false)
      router.push(`/bookings/${response.data.id}`)
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : 'Failed to create booking'
      toast.error(message)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="w-full sm:max-w-md flex flex-col">
        <SheetHeader>
          <SheetTitle className="flex items-center gap-2">
            <ShoppingCart className="h-5 w-5" />
            Booking Cart
            {items.length > 0 && (
              <Badge variant="secondary" className="ml-1">{items.length}</Badge>
            )}
          </SheetTitle>
        </SheetHeader>

        <div className="flex-1 overflow-y-auto space-y-4 py-4">
          {items.length === 0 ? (
            <p className="text-sm text-muted-foreground text-center py-8">
              No items yet. Click &quot;Add to Booking&quot; on provider cards in the chat.
            </p>
          ) : (
            <>
              {/* Booking Items */}
              <div className="space-y-2">
                {items.map(item => {
                  const Icon = TYPE_ICONS[item.type] ?? Building2
                  return (
                    <div key={item.id} className="flex items-center gap-3 p-3 rounded-lg border border-border/50 bg-muted/30">
                      <Icon className="h-5 w-5 text-primary shrink-0" />
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium truncate">{item.name}</p>
                        <p className="text-xs text-muted-foreground">{TYPE_LABELS[item.type] || item.type}</p>
                      </div>
                      <span className="text-sm font-semibold text-primary shrink-0">
                        {formatCurrency(item.price)}
                      </span>
                      <Button
                        variant="ghost"
                        size="sm"
                        className="h-7 w-7 p-0 text-destructive hover:text-destructive"
                        onClick={() => onRemoveItem(item.id)}
                      >
                        <Trash2 className="h-3.5 w-3.5" />
                      </Button>
                    </div>
                  )
                })}
              </div>

              {/* Date Selection */}
              <div className="space-y-3 pt-2 border-t">
                <div className="space-y-1.5">
                  <Label htmlFor="start-date" className="text-xs">Start Date</Label>
                  <Input
                    id="start-date"
                    type="date"
                    value={startDate}
                    onChange={e => setStartDate(e.target.value)}
                    min={new Date().toISOString().split('T')[0]}
                  />
                </div>
                <div className="space-y-1.5">
                  <Label htmlFor="end-date" className="text-xs">End Date</Label>
                  <Input
                    id="end-date"
                    type="date"
                    value={endDate}
                    onChange={e => setEndDate(e.target.value)}
                    min={startDate || new Date().toISOString().split('T')[0]}
                  />
                </div>
                <div className="space-y-1.5">
                  <Label htmlFor="notes" className="text-xs">Notes (optional)</Label>
                  <Input
                    id="notes"
                    placeholder="Any special requests..."
                    value={notes}
                    onChange={e => setNotes(e.target.value)}
                  />
                </div>
              </div>

              {/* Total */}
              <div className="flex items-center justify-between pt-2 border-t">
                <span className="text-sm font-medium">Total</span>
                <span className="text-lg font-bold text-primary">{formatCurrency(totalAmount)}</span>
              </div>
            </>
          )}
        </div>

        {items.length > 0 && (
          <SheetFooter className="flex-col gap-2 sm:flex-col">
            <Button
              className="w-full"
              onClick={handleSubmitBooking}
              disabled={isSubmitting || !startDate || !endDate}
            >
              {isSubmitting ? (
                <>
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                  Creating Booking...
                </>
              ) : (
                `Confirm Booking (${formatCurrency(totalAmount)})`
              )}
            </Button>
            <Button variant="ghost" size="sm" className="w-full text-destructive" onClick={onClearAll}>
              Clear All
            </Button>
          </SheetFooter>
        )}
      </SheetContent>
    </Sheet>
  )
}

function mapProviderType(type: string): ProviderType {
  switch (type) {
    case 'HOTEL': return 'HOTEL'
    case 'TOUR_GUIDE': return 'TOUR_GUIDE'
    case 'VEHICLE': return 'VEHICLE'
    default: return 'HOTEL'
  }
}

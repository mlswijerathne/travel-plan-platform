'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { useCreateBooking } from '@/hooks/use-bookings'
import { searchHotels } from '@/lib/api/hotel'
import { searchGuides } from '@/lib/api/guide'
import { searchVehicles } from '@/lib/api/vehicles'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent } from '@/components/ui/card'
import {
  ArrowLeft,
  ArrowRight,
  Calendar,
  Hotel,
  MapPin,
  Car,
  Check,
  X,
  Loader2,
  Star,
} from 'lucide-react'
import type { BookingItemRequest, ProviderType } from '@/types/booking'

type WizardStep = 'dates' | 'hotel' | 'guide' | 'vehicle' | 'review'

const STEPS: { key: WizardStep; label: string; icon: any }[] = [
  { key: 'dates', label: 'Dates', icon: Calendar },
  { key: 'hotel', label: 'Hotel', icon: Hotel },
  { key: 'guide', label: 'Guide', icon: MapPin },
  { key: 'vehicle', label: 'Vehicle', icon: Car },
  { key: 'review', label: 'Review', icon: Check },
]

interface SelectedItem {
  providerType: ProviderType
  providerId: number
  itemName: string
  unitPrice: number
  quantity: number
}

export default function NewBookingPage() {
  const router = useRouter()
  const createBooking = useCreateBooking()

  const [step, setStep] = useState<WizardStep>('dates')
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const [notes, setNotes] = useState('')
  const [selectedItems, setSelectedItems] = useState<SelectedItem[]>([])

  // Search results
  const [hotels, setHotels] = useState<any[]>([])
  const [guides, setGuides] = useState<any[]>([])
  const [vehicles, setVehicles] = useState<any[]>([])
  const [searching, setSearching] = useState(false)

  const stepIndex = STEPS.findIndex((s) => s.key === step)

  const addItem = (item: SelectedItem) => {
    setSelectedItems((prev) => {
      const exists = prev.find(
        (i) => i.providerType === item.providerType && i.providerId === item.providerId
      )
      if (exists) return prev
      return [...prev, item]
    })
  }

  const removeItem = (providerType: ProviderType, providerId: number) => {
    setSelectedItems((prev) =>
      prev.filter((i) => !(i.providerType === providerType && i.providerId === providerId))
    )
  }

  const isSelected = (providerType: ProviderType, providerId: number) =>
    selectedItems.some((i) => i.providerType === providerType && i.providerId === providerId)

  const totalAmount = selectedItems.reduce((sum, i) => sum + i.unitPrice * i.quantity, 0)

  const numDays = startDate && endDate
    ? Math.max(1, Math.ceil((new Date(endDate).getTime() - new Date(startDate).getTime()) / (1000 * 60 * 60 * 24)))
    : 1

  const searchProviders = async (type: 'hotel' | 'guide' | 'vehicle') => {
    setSearching(true)
    try {
      if (type === 'hotel') {
        const res = await searchHotels({ size: 20 })
        setHotels(res.data ?? [])
      } else if (type === 'guide') {
        const res = await searchGuides({ size: 20 })
        setGuides(res.data ?? [])
      } else {
        const res = await searchVehicles({ size: 20 })
        setVehicles((res as any).content ?? [])
      }
    } catch (err) {
      console.error(`Failed to search ${type}s:`, err)
    } finally {
      setSearching(false)
    }
  }

  const goNext = () => {
    const nextIndex = stepIndex + 1
    if (nextIndex < STEPS.length) {
      const nextStep = STEPS[nextIndex].key
      if (nextStep === 'hotel') searchProviders('hotel')
      if (nextStep === 'guide') searchProviders('guide')
      if (nextStep === 'vehicle') searchProviders('vehicle')
      setStep(nextStep)
    }
  }

  const goBack = () => {
    const prevIndex = stepIndex - 1
    if (prevIndex >= 0) setStep(STEPS[prevIndex].key)
  }

  const handleSubmit = async () => {
    const items: BookingItemRequest[] = selectedItems.map((i) => ({
      providerType: i.providerType,
      providerId: i.providerId,
      itemName: i.itemName,
      quantity: i.quantity,
      unitPrice: i.unitPrice,
      startDate,
      endDate,
    }))

    try {
      const result = await createBooking.mutateAsync({
        startDate,
        endDate,
        notes: notes || undefined,
        items,
      })
      const bookingId = (result as any)?.data?.id ?? (result as any)?.id
      router.push(bookingId ? `/bookings/${bookingId}` : '/bookings')
    } catch (err) {
      console.error('Failed to create booking:', err)
    }
  }

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      <Link href="/bookings" className="flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground">
        <ArrowLeft className="h-4 w-4" /> Back to Bookings
      </Link>

      <div>
        <h1 className="text-2xl font-bold">Create New Booking</h1>
        <p className="text-muted-foreground">Select your travel dates and choose services</p>
      </div>

      {/* Step Indicator */}
      <div className="flex items-center gap-2">
        {STEPS.map((s, i) => {
          const Icon = s.icon
          const isActive = i === stepIndex
          const isDone = i < stepIndex
          return (
            <div key={s.key} className="flex items-center gap-2">
              {i > 0 && <div className={`h-px w-6 ${isDone ? 'bg-primary' : 'bg-border'}`} />}
              <div
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-medium transition-colors ${
                  isActive ? 'bg-primary text-white' : isDone ? 'bg-primary/10 text-primary' : 'bg-muted text-muted-foreground'
                }`}
              >
                <Icon className="h-3.5 w-3.5" />
                <span className="hidden sm:inline">{s.label}</span>
              </div>
            </div>
          )
        })}
      </div>

      {/* Step Content */}
      <div className="bg-card rounded-xl border p-6">
        {/* DATES STEP */}
        {step === 'dates' && (
          <div className="space-y-4">
            <h2 className="text-lg font-semibold">Select Travel Dates</h2>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>Start Date</Label>
                <Input
                  type="date"
                  value={startDate}
                  min={new Date().toISOString().split('T')[0]}
                  onChange={(e) => setStartDate(e.target.value)}
                  required
                />
              </div>
              <div className="space-y-2">
                <Label>End Date</Label>
                <Input
                  type="date"
                  value={endDate}
                  min={startDate || new Date().toISOString().split('T')[0]}
                  onChange={(e) => setEndDate(e.target.value)}
                  required
                />
              </div>
            </div>
            {startDate && endDate && (
              <p className="text-sm text-muted-foreground">
                Trip duration: <strong>{numDays} day{numDays > 1 ? 's' : ''}</strong>
              </p>
            )}
            <div className="space-y-2">
              <Label>Notes (optional)</Label>
              <Input
                placeholder="Any special requests..."
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
              />
            </div>
          </div>
        )}

        {/* HOTEL STEP */}
        {step === 'hotel' && (
          <div className="space-y-4">
            <h2 className="text-lg font-semibold">Choose a Hotel (optional)</h2>
            <p className="text-sm text-muted-foreground">Select a hotel for your stay, or skip this step.</p>
            {searching ? (
              <div className="flex items-center justify-center py-8">
                <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
              </div>
            ) : hotels.length === 0 ? (
              <p className="text-center py-8 text-muted-foreground">No hotels available</p>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3 max-h-[400px] overflow-y-auto">
                {hotels.map((hotel: any) => {
                  const selected = isSelected('HOTEL', hotel.id)
                  return (
                    <Card
                      key={hotel.id}
                      className={`cursor-pointer transition-all ${selected ? 'ring-2 ring-primary' : 'hover:bg-accent/50'}`}
                      onClick={() =>
                        selected
                          ? removeItem('HOTEL', hotel.id)
                          : addItem({
                              providerType: 'HOTEL',
                              providerId: hotel.id,
                              itemName: hotel.name,
                              unitPrice: hotel.minPrice ?? hotel.basePrice ?? 5000,
                              quantity: numDays,
                            })
                      }
                    >
                      <CardContent className="p-4 flex items-center gap-3">
                        <Hotel className="h-8 w-8 text-primary shrink-0" />
                        <div className="flex-1 min-w-0">
                          <p className="font-medium text-sm truncate">{hotel.name}</p>
                          <p className="text-xs text-muted-foreground">{hotel.city} · {hotel.starRating}★</p>
                        </div>
                        {hotel.averageRating > 0 && (
                          <div className="flex items-center gap-1 text-xs">
                            <Star className="h-3 w-3 fill-amber-400 text-amber-400" />
                            {hotel.averageRating.toFixed(1)}
                          </div>
                        )}
                        {selected && <Check className="h-5 w-5 text-primary shrink-0" />}
                      </CardContent>
                    </Card>
                  )
                })}
              </div>
            )}
          </div>
        )}

        {/* GUIDE STEP */}
        {step === 'guide' && (
          <div className="space-y-4">
            <h2 className="text-lg font-semibold">Choose a Tour Guide (optional)</h2>
            <p className="text-sm text-muted-foreground">Select a guide for your trip, or skip this step.</p>
            {searching ? (
              <div className="flex items-center justify-center py-8">
                <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
              </div>
            ) : guides.length === 0 ? (
              <p className="text-center py-8 text-muted-foreground">No guides available</p>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3 max-h-[400px] overflow-y-auto">
                {guides.map((guide: any) => {
                  const selected = isSelected('TOUR_GUIDE', guide.id)
                  return (
                    <Card
                      key={guide.id}
                      className={`cursor-pointer transition-all ${selected ? 'ring-2 ring-primary' : 'hover:bg-accent/50'}`}
                      onClick={() =>
                        selected
                          ? removeItem('TOUR_GUIDE', guide.id)
                          : addItem({
                              providerType: 'TOUR_GUIDE',
                              providerId: guide.id,
                              itemName: `${guide.firstName} ${guide.lastName}`,
                              unitPrice: (guide.dailyRate ?? (guide.hourlyRate * 8)) || 3000,
                              quantity: numDays,
                            })
                      }
                    >
                      <CardContent className="p-4 flex items-center gap-3">
                        <MapPin className="h-8 w-8 text-primary shrink-0" />
                        <div className="flex-1 min-w-0">
                          <p className="font-medium text-sm truncate">{guide.firstName} {guide.lastName}</p>
                          <p className="text-xs text-muted-foreground">
                            {guide.languages?.join(', ')} · {guide.experienceYears}yr exp
                          </p>
                        </div>
                        {guide.averageRating > 0 && (
                          <div className="flex items-center gap-1 text-xs">
                            <Star className="h-3 w-3 fill-amber-400 text-amber-400" />
                            {guide.averageRating.toFixed(1)}
                          </div>
                        )}
                        {selected && <Check className="h-5 w-5 text-primary shrink-0" />}
                      </CardContent>
                    </Card>
                  )
                })}
              </div>
            )}
          </div>
        )}

        {/* VEHICLE STEP */}
        {step === 'vehicle' && (
          <div className="space-y-4">
            <h2 className="text-lg font-semibold">Choose a Vehicle (optional)</h2>
            <p className="text-sm text-muted-foreground">Select a vehicle for transport, or skip this step.</p>
            {searching ? (
              <div className="flex items-center justify-center py-8">
                <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
              </div>
            ) : vehicles.length === 0 ? (
              <p className="text-center py-8 text-muted-foreground">No vehicles available</p>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3 max-h-[400px] overflow-y-auto">
                {vehicles.map((vehicle: any) => {
                  const selected = isSelected('VEHICLE', vehicle.id)
                  const price = vehicle.dailyRate ?? 0
                  return (
                    <Card
                      key={vehicle.id}
                      className={`cursor-pointer transition-all ${selected ? 'ring-2 ring-primary' : 'hover:bg-accent/50'}`}
                      onClick={() =>
                        selected
                          ? removeItem('VEHICLE', vehicle.id)
                          : addItem({
                              providerType: 'VEHICLE',
                              providerId: vehicle.id,
                              itemName: `${vehicle.make} ${vehicle.model}`,
                              unitPrice: price,
                              quantity: numDays,
                            })
                      }
                    >
                      <CardContent className="p-4 flex items-center gap-3">
                        <Car className="h-8 w-8 text-primary shrink-0" />
                        <div className="flex-1 min-w-0">
                          <p className="font-medium text-sm truncate">{vehicle.make} {vehicle.model}</p>
                          <p className="text-xs text-muted-foreground">
                            {vehicle.vehicleType} · {vehicle.seatingCapacity} seats · Rs. {price}/day
                          </p>
                        </div>
                        {selected && <Check className="h-5 w-5 text-primary shrink-0" />}
                      </CardContent>
                    </Card>
                  )
                })}
              </div>
            )}
          </div>
        )}

        {/* REVIEW STEP */}
        {step === 'review' && (
          <div className="space-y-4">
            <h2 className="text-lg font-semibold">Review Your Booking</h2>

            <div className="grid grid-cols-2 gap-4 text-sm">
              <div>
                <p className="text-muted-foreground">Start Date</p>
                <p className="font-medium">{startDate}</p>
              </div>
              <div>
                <p className="text-muted-foreground">End Date</p>
                <p className="font-medium">{endDate}</p>
              </div>
              <div>
                <p className="text-muted-foreground">Duration</p>
                <p className="font-medium">{numDays} day{numDays > 1 ? 's' : ''}</p>
              </div>
              {notes && (
                <div>
                  <p className="text-muted-foreground">Notes</p>
                  <p className="font-medium">{notes}</p>
                </div>
              )}
            </div>

            {selectedItems.length === 0 ? (
              <div className="text-center py-6 border rounded-lg">
                <p className="text-muted-foreground">No items selected. Go back and add at least one service.</p>
              </div>
            ) : (
              <div className="space-y-2">
                {selectedItems.map((item) => (
                  <div key={`${item.providerType}-${item.providerId}`} className="flex items-center gap-3 p-3 rounded-lg border">
                    <Badge variant="outline" className="text-xs">{item.providerType}</Badge>
                    <div className="flex-1 min-w-0">
                      <p className="font-medium text-sm">{item.itemName}</p>
                      <p className="text-xs text-muted-foreground">
                        Rs. {item.unitPrice} x {item.quantity} day{item.quantity > 1 ? 's' : ''}
                      </p>
                    </div>
                    <p className="font-semibold text-sm">Rs. {(item.unitPrice * item.quantity).toLocaleString()}</p>
                    <Button
                      variant="ghost"
                      size="icon"
                      className="h-7 w-7 text-destructive"
                      onClick={() => removeItem(item.providerType, item.providerId)}
                    >
                      <X className="h-4 w-4" />
                    </Button>
                  </div>
                ))}

                <div className="flex justify-between items-center pt-4 border-t">
                  <p className="text-lg font-semibold">Total</p>
                  <p className="text-2xl font-bold text-primary">Rs. {totalAmount.toLocaleString()}</p>
                </div>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Navigation Buttons */}
      <div className="flex justify-between">
        <Button variant="outline" onClick={goBack} disabled={stepIndex === 0}>
          <ArrowLeft className="h-4 w-4 mr-2" /> Back
        </Button>

        {step === 'review' ? (
          <Button
            onClick={handleSubmit}
            disabled={selectedItems.length === 0 || createBooking.isPending}
          >
            {createBooking.isPending ? (
              <>
                <Loader2 className="h-4 w-4 mr-2 animate-spin" /> Creating...
              </>
            ) : (
              <>
                <Check className="h-4 w-4 mr-2" /> Confirm Booking
              </>
            )}
          </Button>
        ) : (
          <Button onClick={goNext} disabled={step === 'dates' && (!startDate || !endDate)}>
            {step === 'dates' ? 'Choose Services' : 'Next'}
            <ArrowRight className="h-4 w-4 ml-2" />
          </Button>
        )}
      </div>
    </div>
  )
}

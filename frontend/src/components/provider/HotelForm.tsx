'use client'

import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { HOTEL_AMENITIES } from '@/types/hotel'
import { formatAmenity } from '@/lib/utils'
import { uploadImage } from '@/lib/api/images'
import type { Hotel } from '@/types/hotel'

const hotelSchema = z.object({
  name: z.string().min(1, 'Name is required').max(255),
  description: z.string().max(2000).optional(),
  address: z.string().min(1, 'Address is required'),
  city: z.string().min(1, 'City is required').max(100),
  starRating: z.coerce.number().min(1).max(5).optional(),
  checkInTime: z.string().optional(),
  checkOutTime: z.string().optional(),
  amenities: z.array(z.string()).optional(),
})

type HotelFormData = z.infer<typeof hotelSchema>

interface HotelFormProps {
  hotel?: Hotel
  onSubmit: (data: HotelFormData & { imageUrl?: string }) => void
  isPending: boolean
}

export function HotelForm({ hotel, onSubmit, isPending }: HotelFormProps) {
  const [imageFile, setImageFile] = useState<File | null>(null)
  const [uploading, setUploading] = useState(false)
  const { register, handleSubmit, watch, setValue, formState: { errors } } = useForm<HotelFormData>({
    resolver: zodResolver(hotelSchema),
    defaultValues: {
      name: hotel?.name ?? '',
      description: hotel?.description ?? '',
      address: hotel?.address ?? '',
      city: hotel?.city ?? '',
      starRating: hotel?.starRating ?? undefined,
      checkInTime: hotel?.checkInTime ?? '14:00',
      checkOutTime: hotel?.checkOutTime ?? '11:00',
      amenities: hotel?.amenities ?? [],
    },
  })

  const selectedAmenities = watch('amenities') ?? []

  async function handleFormSubmit(data: HotelFormData) {
    let imageUrl = hotel?.imageUrl ?? undefined
    if (imageFile) {
      setUploading(true)
      try {
        imageUrl = await uploadImage(imageFile, 'hotels')
      } finally {
        setUploading(false)
      }
    }
    onSubmit({ ...data, imageUrl })
  }

  function toggleAmenity(amenity: string) {
    const current = selectedAmenities
    const updated = current.includes(amenity)
      ? current.filter((a) => a !== amenity)
      : [...current, amenity]
    setValue('amenities', updated)
  }

  return (
    <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-6">
      <div>
        <label className="text-sm font-medium mb-1 block">Hotel Image</label>
        {hotel?.imageUrl && !imageFile && (
          <img src={hotel.imageUrl} alt="Current hotel" className="h-32 w-full object-cover rounded-lg mb-2" />
        )}
        <Input
          type="file"
          accept="image/*"
          onChange={(e) => e.target.files?.[0] && setImageFile(e.target.files[0])}
        />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label className="text-sm font-medium mb-1 block">Hotel Name *</label>
          <Input {...register('name')} placeholder="Hotel name" />
          {errors.name && <p className="text-xs text-destructive mt-1">{errors.name.message}</p>}
        </div>
        <div>
          <label className="text-sm font-medium mb-1 block">City *</label>
          <Input {...register('city')} placeholder="City" />
          {errors.city && <p className="text-xs text-destructive mt-1">{errors.city.message}</p>}
        </div>
      </div>

      <div>
        <label className="text-sm font-medium mb-1 block">Address *</label>
        <Input {...register('address')} placeholder="Full address" />
        {errors.address && <p className="text-xs text-destructive mt-1">{errors.address.message}</p>}
      </div>

      <div>
        <label className="text-sm font-medium mb-1 block">Description</label>
        <Textarea {...register('description')} placeholder="Describe your hotel..." rows={4} />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div>
          <label className="text-sm font-medium mb-1 block">Star Rating</label>
          <Input type="number" {...register('starRating')} min={1} max={5} placeholder="1-5" />
        </div>
        <div>
          <label className="text-sm font-medium mb-1 block">Check-in Time</label>
          <Input type="time" {...register('checkInTime')} />
        </div>
        <div>
          <label className="text-sm font-medium mb-1 block">Check-out Time</label>
          <Input type="time" {...register('checkOutTime')} />
        </div>
      </div>

      <div>
        <label className="text-sm font-medium mb-2 block">Amenities</label>
        <div className="flex flex-wrap gap-2">
          {HOTEL_AMENITIES.map((a) => (
            <Button
              key={a}
              type="button"
              variant={selectedAmenities.includes(a) ? 'default' : 'outline'}
              size="sm"
              onClick={() => toggleAmenity(a)}
            >
              {formatAmenity(a)}
            </Button>
          ))}
        </div>
      </div>

      <div className="flex justify-end gap-3">
        <Button type="submit" disabled={isPending || uploading}>
          {uploading ? 'Uploading image...' : isPending ? 'Saving...' : hotel ? 'Update Hotel' : 'Create Hotel'}
        </Button>
      </div>
    </form>
  )
}

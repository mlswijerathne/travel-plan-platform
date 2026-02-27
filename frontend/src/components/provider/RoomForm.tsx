'use client'

import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { ROOM_TYPES } from '@/types/hotel'
import type { Room } from '@/types/hotel'

const roomSchema = z.object({
  roomType: z.string().min(1, 'Room type is required'),
  name: z.string().min(1, 'Name is required').max(100),
  description: z.string().max(1000).optional(),
  pricePerNight: z.coerce.number().min(0.01, 'Price must be positive'),
  maxOccupancy: z.coerce.number().min(1, 'At least 1 guest'),
  totalRooms: z.coerce.number().min(1, 'At least 1 room'),
})

type RoomFormData = z.infer<typeof roomSchema>

interface RoomFormProps {
  room?: Room
  onSubmit: (data: RoomFormData) => void
  isPending: boolean
  onCancel: () => void
}

export function RoomForm({ room, onSubmit, isPending, onCancel }: RoomFormProps) {
  const { register, handleSubmit, formState: { errors } } = useForm<RoomFormData>({
    resolver: zodResolver(roomSchema),
    defaultValues: {
      roomType: room?.roomType ?? 'STANDARD',
      name: room?.name ?? '',
      description: room?.description ?? '',
      pricePerNight: room?.pricePerNight ?? 0,
      maxOccupancy: room?.maxOccupancy ?? 2,
      totalRooms: room?.totalRooms ?? 1,
    },
  })

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 p-4 border rounded-lg bg-muted/20">
      <h4 className="font-medium">{room ? 'Edit Room' : 'Add Room'}</h4>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
        <div>
          <label className="text-sm text-muted-foreground mb-1 block">Room Type *</label>
          <select {...register('roomType')} className="w-full h-9 rounded-md border bg-background px-3 text-sm">
            {ROOM_TYPES.map((t) => (
              <option key={t.value} value={t.value}>{t.label}</option>
            ))}
          </select>
          {errors.roomType && <p className="text-xs text-destructive mt-1">{errors.roomType.message}</p>}
        </div>
        <div>
          <label className="text-sm text-muted-foreground mb-1 block">Room Name *</label>
          <Input {...register('name')} placeholder="e.g. Ocean View Deluxe" />
          {errors.name && <p className="text-xs text-destructive mt-1">{errors.name.message}</p>}
        </div>
      </div>

      <div>
        <label className="text-sm text-muted-foreground mb-1 block">Description</label>
        <Textarea {...register('description')} placeholder="Room description..." rows={2} />
      </div>

      <div className="grid grid-cols-3 gap-3">
        <div>
          <label className="text-sm text-muted-foreground mb-1 block">Price/Night *</label>
          <Input type="number" step="0.01" {...register('pricePerNight')} />
          {errors.pricePerNight && <p className="text-xs text-destructive mt-1">{errors.pricePerNight.message}</p>}
        </div>
        <div>
          <label className="text-sm text-muted-foreground mb-1 block">Max Guests *</label>
          <Input type="number" {...register('maxOccupancy')} />
        </div>
        <div>
          <label className="text-sm text-muted-foreground mb-1 block">Total Rooms *</label>
          <Input type="number" {...register('totalRooms')} />
        </div>
      </div>

      <div className="flex justify-end gap-2">
        <Button type="button" variant="outline" size="sm" onClick={onCancel}>Cancel</Button>
        <Button type="submit" size="sm" disabled={isPending}>
          {isPending ? 'Saving...' : room ? 'Update' : 'Add Room'}
        </Button>
      </div>
    </form>
  )
}

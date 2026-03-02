'use client'

import { useState } from 'react'
import { useRoomsByHotel, useCreateRoom, useUpdateRoom, useDeleteRoom } from '@/hooks/use-rooms'
import { RoomForm } from './RoomForm'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Plus, Pencil, Trash2, Users } from 'lucide-react'
import { formatCurrency } from '@/lib/utils'
import type { Room } from '@/types/hotel'

export function RoomManagement({ hotelId }: { hotelId: number }) {
  const { data, isLoading } = useRoomsByHotel(hotelId)
  const createMutation = useCreateRoom()
  const updateMutation = useUpdateRoom(hotelId)
  const deleteMutation = useDeleteRoom(hotelId)

  const [showForm, setShowForm] = useState(false)
  const [editingRoom, setEditingRoom] = useState<Room | null>(null)

  const rooms = data?.data ?? []

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="font-semibold">Rooms ({rooms.length})</h3>
        <Button size="sm" onClick={() => { setShowForm(true); setEditingRoom(null) }}>
          <Plus className="h-3.5 w-3.5 mr-1" />
          Add Room
        </Button>
      </div>

      {(showForm || editingRoom) && (
        <RoomForm
          room={editingRoom ?? undefined}
          isPending={createMutation.isPending || updateMutation.isPending}
          onCancel={() => { setShowForm(false); setEditingRoom(null) }}
          onSubmit={(data) => {
            if (editingRoom) {
              updateMutation.mutate(
                { id: editingRoom.id, data },
                { onSuccess: () => setEditingRoom(null) }
              )
            } else {
              createMutation.mutate(
                { ...data, hotelId },
                { onSuccess: () => setShowForm(false) }
              )
            }
          }}
        />
      )}

      {isLoading ? (
        <div className="space-y-2">
          {[1, 2].map((i) => <div key={i} className="h-16 bg-muted rounded-lg animate-pulse" />)}
        </div>
      ) : rooms.length === 0 ? (
        <p className="text-sm text-muted-foreground text-center py-8">No rooms yet. Add your first room above.</p>
      ) : (
        <div className="space-y-2">
          {rooms.map((room) => (
            <div key={room.id} className="flex items-center gap-3 p-3 border rounded-lg bg-card">
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2">
                  <p className="font-medium text-sm">{room.name}</p>
                  <Badge variant="outline" className="text-[10px]">{room.roomType}</Badge>
                  {!room.isActive && <Badge variant="destructive" className="text-[10px]">Inactive</Badge>}
                </div>
                <div className="flex items-center gap-3 text-xs text-muted-foreground mt-0.5">
                  <span>{formatCurrency(room.pricePerNight)}/night</span>
                  <span className="flex items-center gap-0.5"><Users className="h-3 w-3" />{room.maxOccupancy}</span>
                  <span>{room.totalRooms} rooms</span>
                </div>
              </div>
              <div className="flex gap-1">
                <Button variant="ghost" size="sm" className="h-8 w-8 p-0" onClick={() => { setEditingRoom(room); setShowForm(false) }}>
                  <Pencil className="h-3.5 w-3.5" />
                </Button>
                <Button
                  variant="ghost"
                  size="sm"
                  className="h-8 w-8 p-0 text-destructive"
                  onClick={() => deleteMutation.mutate(room.id)}
                  disabled={deleteMutation.isPending}
                >
                  <Trash2 className="h-3.5 w-3.5" />
                </Button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

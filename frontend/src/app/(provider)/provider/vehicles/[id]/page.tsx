'use client'

import { use, useState } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { useVehicle, useUpdateVehicle } from '@/hooks/use-vehicles'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Switch } from '@/components/ui/switch'
import { ArrowLeft } from 'lucide-react'
import { VEHICLE_TYPES, type Vehicle, type VehicleType } from '@/types/vehicle'

export default function EditVehiclePage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params)
  const { data: vehicle, isLoading } = useVehicle(Number(id))

  if (isLoading) {
    return (
      <div className="space-y-4 max-w-2xl">
        <div className="h-6 bg-muted rounded w-32 animate-pulse" />
        <div className="h-8 bg-muted rounded w-64 animate-pulse" />
        <div className="h-96 bg-muted rounded-xl animate-pulse" />
      </div>
    )
  }

  if (!vehicle) {
    return (
      <div className="text-center py-12">
        <p className="text-muted-foreground mb-3">Vehicle not found</p>
        <Link href="/provider/vehicles"><Button variant="outline">Go Back</Button></Link>
      </div>
    )
  }

  return <VehicleEditForm vehicle={vehicle} />
}

function VehicleEditForm({ vehicle }: { vehicle: Vehicle }) {
  const router = useRouter()
  const updateMutation = useUpdateVehicle()
  const v = vehicle as any

  const [form, setForm] = useState({
    make: v.make ?? '',
    model: v.model ?? '',
    year: v.year ?? new Date().getFullYear(),
    vehicleType: v.vehicleType ?? 'CAR',
    licensePlate: v.licensePlate ?? '',
    seatingCapacity: v.seatingCapacity ?? 4,
    dailyRate: v.dailyRate ?? 0,
    isAvailable: v.isAvailable ?? true,
  })

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    try {
      await updateMutation.mutateAsync({
        id: vehicle.id,
        data: {
          ...form,
          vehicleType: form.vehicleType as VehicleType,
          seatingCapacity: Number(form.seatingCapacity),
          dailyRate: Number(form.dailyRate),
          year: Number(form.year),
        },
      })
      router.push('/provider/vehicles')
    } catch (err) {
      console.error('Failed to update vehicle:', err)
    }
  }

  return (
    <div className="space-y-6 max-w-2xl">
      <Link href="/provider/vehicles" className="flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground">
        <ArrowLeft className="h-4 w-4" /> Back to My Vehicles
      </Link>

      <div>
        <h1 className="text-2xl font-bold">Edit Vehicle</h1>
        <p className="text-muted-foreground">{form.make} {form.model}</p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4 bg-card rounded-xl border p-6">
        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label>Make</Label>
            <Input value={form.make} onChange={(e) => setForm({ ...form, make: e.target.value })} required />
          </div>
          <div className="space-y-2">
            <Label>Model</Label>
            <Input value={form.model} onChange={(e) => setForm({ ...form, model: e.target.value })} required />
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label>Vehicle Type</Label>
            <Select value={form.vehicleType} onValueChange={(val) => setForm({ ...form, vehicleType: val })}>
              <SelectTrigger><SelectValue /></SelectTrigger>
              <SelectContent>
                {VEHICLE_TYPES.map((t) => (
                  <SelectItem key={t} value={t}>{t.replace('_', ' ')}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-2">
            <Label>Year</Label>
            <Input type="number" value={form.year} onChange={(e) => setForm({ ...form, year: Number(e.target.value) })} required />
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label>License Plate</Label>
            <Input value={form.licensePlate} onChange={(e) => setForm({ ...form, licensePlate: e.target.value })} required />
          </div>
          <div className="space-y-2">
            <Label>Seating Capacity</Label>
            <Input type="number" value={form.seatingCapacity} onChange={(e) => setForm({ ...form, seatingCapacity: Number(e.target.value) })} required />
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label>Daily Rate (Rs)</Label>
            <Input type="number" step="0.01" value={form.dailyRate} onChange={(e) => setForm({ ...form, dailyRate: Number(e.target.value) })} required />
          </div>
        </div>

        <div className="flex items-center gap-3 pt-2">
          <Switch
            checked={form.isAvailable}
            onCheckedChange={(checked) => setForm({ ...form, isAvailable: checked })}
          />
          <Label>Available for rental</Label>
        </div>

        <div className="flex justify-end gap-3 pt-4">
          <Link href="/provider/vehicles">
            <Button type="button" variant="outline">Cancel</Button>
          </Link>
          <Button type="submit" disabled={updateMutation.isPending}>
            {updateMutation.isPending ? 'Saving...' : 'Save Changes'}
          </Button>
        </div>
      </form>
    </div>
  )
}

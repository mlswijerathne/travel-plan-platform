'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { useCreateVehicle } from '@/hooks/use-vehicles'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { ArrowLeft } from 'lucide-react'
import { VEHICLE_TYPES } from '@/types/vehicle'

export default function NewVehiclePage() {
  const router = useRouter()
  const createMutation = useCreateVehicle()

  const [form, setForm] = useState({
    make: '',
    model: '',
    year: new Date().getFullYear(),
    vehicleType: 'CAR' as string,
    licensePlate: '',
    capacity: 4,
    pricePerDay: '',
    description: '',
    city: '',
  })

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    try {
      await createMutation.mutateAsync({
        ...form,
        capacity: Number(form.capacity),
        pricePerDay: Number(form.pricePerDay),
        year: Number(form.year),
      } as any)
      router.push('/provider/vehicles')
    } catch (err) {
      console.error('Failed to create vehicle:', err)
    }
  }

  return (
    <div className="space-y-6 max-w-2xl">
      <Link href="/provider/vehicles" className="flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground">
        <ArrowLeft className="h-4 w-4" /> Back to My Vehicles
      </Link>

      <div>
        <h1 className="text-2xl font-bold">Add New Vehicle</h1>
        <p className="text-muted-foreground">List a new vehicle for rental</p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4 bg-card rounded-xl border p-6">
        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label>Make</Label>
            <Input placeholder="Toyota" value={form.make} onChange={(e) => setForm({ ...form, make: e.target.value })} required />
          </div>
          <div className="space-y-2">
            <Label>Model</Label>
            <Input placeholder="Prius" value={form.model} onChange={(e) => setForm({ ...form, model: e.target.value })} required />
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
            <Input placeholder="ABC-1234" value={form.licensePlate} onChange={(e) => setForm({ ...form, licensePlate: e.target.value })} required />
          </div>
          <div className="space-y-2">
            <Label>Seating Capacity</Label>
            <Input type="number" value={form.capacity} onChange={(e) => setForm({ ...form, capacity: Number(e.target.value) })} required />
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label>Daily Rate (Rs)</Label>
            <Input type="number" step="0.01" placeholder="5000" value={form.pricePerDay} onChange={(e) => setForm({ ...form, pricePerDay: e.target.value })} required />
          </div>
          <div className="space-y-2">
            <Label>City</Label>
            <Input placeholder="Colombo" value={form.city} onChange={(e) => setForm({ ...form, city: e.target.value })} />
          </div>
        </div>

        <div className="space-y-2">
          <Label>Description</Label>
          <Input placeholder="Brief description of the vehicle" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
        </div>

        <div className="flex justify-end gap-3 pt-4">
          <Link href="/provider/vehicles">
            <Button type="button" variant="outline">Cancel</Button>
          </Link>
          <Button type="submit" disabled={createMutation.isPending}>
            {createMutation.isPending ? 'Creating...' : 'Add Vehicle'}
          </Button>
        </div>
      </form>
    </div>
  )
}

'use client'

import Link from 'next/link'
import { useMyVehicles, useDeleteVehicle } from '@/hooks/use-vehicles'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Car, Plus, Pencil, Trash2, Star } from 'lucide-react'

export default function ProviderVehiclesPage() {
  const { data, isLoading } = useMyVehicles()
  const deleteMutation = useDeleteVehicle()

  const vehicles = (data as any[] | undefined) ?? []

  if (isLoading) {
    return (
      <div className="space-y-4">
        <div className="h-8 bg-muted rounded w-48 animate-pulse" />
        {[1, 2, 3].map((i) => (
          <div key={i} className="h-20 bg-muted rounded-xl animate-pulse" />
        ))}
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">My Vehicles</h1>
          <p className="text-muted-foreground">Manage your vehicle fleet</p>
        </div>
        <Link href="/provider/vehicles/new">
          <Button>
            <Plus className="h-4 w-4 mr-2" />
            Add Vehicle
          </Button>
        </Link>
      </div>

      {vehicles.length === 0 ? (
        <div className="text-center py-12 border rounded-xl">
          <Car className="h-12 w-12 mx-auto text-muted-foreground mb-3" />
          <p className="text-muted-foreground mb-3">No vehicles listed yet</p>
          <Link href="/provider/vehicles/new">
            <Button size="sm">Add Your First Vehicle</Button>
          </Link>
        </div>
      ) : (
        <div className="space-y-2">
          {vehicles.map((vehicle: any) => (
            <div
              key={vehicle.id}
              className="flex items-center gap-4 p-4 rounded-xl border bg-card hover:bg-accent/50 transition-colors"
            >
              <div className="h-10 w-10 rounded-lg bg-primary/10 flex items-center justify-center shrink-0">
                <Car className="h-5 w-5 text-primary" />
              </div>
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2">
                  <p className="font-medium text-sm truncate">
                    {vehicle.make} {vehicle.model} ({vehicle.year})
                  </p>
                  <Badge variant={vehicle.isAvailable ? 'default' : 'secondary'} className="text-xs">
                    {vehicle.isAvailable ? 'Available' : 'Unavailable'}
                  </Badge>
                </div>
                <p className="text-xs text-muted-foreground">
                  {vehicle.vehicleType} · {vehicle.seatingCapacity} seats · Rs. {vehicle.dailyRate}/day
                </p>
              </div>
              {vehicle.reviewCount > 0 && (
                <div className="flex items-center gap-1 text-sm shrink-0">
                  <Star className="h-3 w-3 fill-amber-400 text-amber-400" />
                  <span className="font-medium">{vehicle.averageRating?.toFixed(1)}</span>
                </div>
              )}
              <div className="flex items-center gap-1 shrink-0">
                <Link href={`/provider/vehicles/${vehicle.id}`}>
                  <Button variant="ghost" size="icon" className="h-8 w-8">
                    <Pencil className="h-4 w-4" />
                  </Button>
                </Link>
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-8 w-8 text-destructive hover:text-destructive"
                  onClick={() => {
                    if (confirm('Delete this vehicle?')) {
                      deleteMutation.mutate(vehicle.id)
                    }
                  }}
                >
                  <Trash2 className="h-4 w-4" />
                </Button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

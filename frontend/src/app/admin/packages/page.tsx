'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'
import { getPackages, deletePackage } from '@/lib/api/packages'
import type { TripPackage } from '@/types/trip-package'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from '@/components/ui/alert-dialog'
import { Plus, Package, Pencil, Trash2, MapPin, Clock, Star } from 'lucide-react'
import { toast } from 'sonner'

export default function AdminPackagesPage() {
  const [packages, setPackages] = useState<TripPackage[]>([])
  const [loading, setLoading] = useState(true)
  const [total, setTotal] = useState(0)

  useEffect(() => { load() }, [])

  async function load() {
    try {
      const res = await getPackages({ page: 0, size: 50 })
      setPackages(res.data ?? [])
      setTotal(res.pagination?.totalItems ?? res.data?.length ?? 0)
    } catch {
      toast.error('Failed to load packages')
    } finally {
      setLoading(false)
    }
  }

  async function handleDelete(id: number) {
    try {
      await deletePackage(id)
      toast.success('Package deleted')
      setPackages(prev => prev.filter(p => p.id !== id))
    } catch {
      toast.error('Failed to delete package')
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="font-display text-2xl font-bold">Packages</h1>
          <p className="text-muted-foreground text-sm mt-1">{total} total packages</p>
        </div>
        <Link href="/admin/packages/new">
          <Button className="gap-2">
            <Plus className="h-4 w-4" />
            New Package
          </Button>
        </Link>
      </div>

      {loading ? (
        <div className="space-y-3">
          {[1, 2, 3].map(i => <Skeleton key={i} className="h-28 rounded-xl" />)}
        </div>
      ) : packages.length === 0 ? (
        <Card className="border-dashed">
          <CardContent className="py-16 text-center">
            <Package className="h-12 w-12 text-muted-foreground/30 mx-auto mb-4" />
            <p className="font-medium text-muted-foreground mb-3">No packages yet</p>
            <Link href="/admin/packages/new">
              <Button>Create First Package</Button>
            </Link>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-3">
          {packages.map(pkg => {
            const finalPrice = pkg.basePrice - (pkg.basePrice * ((pkg.discountPercentage ?? 0) / 100))
            return (
              <Card key={pkg.id} className="shadow-sm hover:shadow-md transition-shadow">
                <CardContent className="flex items-center gap-4 py-4">
                  {pkg.images?.[0] ? (
                    <img src={pkg.images[0]} alt={pkg.name} className="h-16 w-24 rounded-lg object-cover shrink-0" />
                  ) : (
                    <div className="h-16 w-24 rounded-lg bg-muted flex items-center justify-center shrink-0">
                      <Package className="h-6 w-6 text-muted-foreground/50" />
                    </div>
                  )}
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1">
                      <h3 className="font-semibold text-sm truncate">{pkg.name}</h3>
                      {pkg.isFeatured && <Badge variant="secondary" className="text-xs shrink-0">Featured</Badge>}
                      {!pkg.isActive && <Badge variant="outline" className="text-xs shrink-0 text-muted-foreground">Inactive</Badge>}
                    </div>
                    <div className="flex items-center gap-4 text-xs text-muted-foreground">
                      <span className="flex items-center gap-1"><Clock className="h-3 w-3" /> {pkg.durationDays} days</span>
                      {pkg.destinations?.length > 0 && (
                        <span className="flex items-center gap-1"><MapPin className="h-3 w-3" /> {pkg.destinations.slice(0, 2).join(', ')}{pkg.destinations.length > 2 ? ` +${pkg.destinations.length - 2}` : ''}</span>
                      )}
                      <span className="flex items-center gap-1"><Star className="h-3 w-3" /> {pkg.maxParticipants} max</span>
                    </div>
                    <p className="text-sm font-medium text-foreground mt-1">
                      Rs {finalPrice.toLocaleString()}
                      {(pkg.discountPercentage ?? 0) > 0 && (
                        <span className="text-xs text-muted-foreground line-through ml-2">Rs {pkg.basePrice.toLocaleString()}</span>
                      )}
                    </p>
                  </div>
                  <div className="flex items-center gap-2 shrink-0">
                    <Link href={`/admin/packages/${pkg.id}/edit`}>
                      <Button variant="outline" size="sm" className="gap-1.5">
                        <Pencil className="h-3.5 w-3.5" />
                        Edit
                      </Button>
                    </Link>
                    <AlertDialog>
                      <AlertDialogTrigger asChild>
                        <Button variant="outline" size="sm" className="gap-1.5 text-destructive border-destructive/20 hover:bg-destructive/5 hover:text-destructive">
                          <Trash2 className="h-3.5 w-3.5" />
                          Delete
                        </Button>
                      </AlertDialogTrigger>
                      <AlertDialogContent>
                        <AlertDialogHeader>
                          <AlertDialogTitle>Delete package?</AlertDialogTitle>
                          <AlertDialogDescription>
                            This will permanently delete &quot;{pkg.name}&quot;. This action cannot be undone.
                          </AlertDialogDescription>
                        </AlertDialogHeader>
                        <AlertDialogFooter>
                          <AlertDialogCancel>Cancel</AlertDialogCancel>
                          <AlertDialogAction onClick={() => handleDelete(pkg.id)} className="bg-destructive hover:bg-destructive/90">
                            Delete
                          </AlertDialogAction>
                        </AlertDialogFooter>
                      </AlertDialogContent>
                    </AlertDialog>
                  </div>
                </CardContent>
              </Card>
            )
          })}
        </div>
      )}
    </div>
  )
}

'use client'

import { useRouter } from 'next/navigation'
import { useCreateHotel } from '@/hooks/use-hotels'
import { HotelForm } from '@/components/provider/HotelForm'
import { ArrowLeft } from 'lucide-react'
import Link from 'next/link'

export default function NewHotelPage() {
  const router = useRouter()
  const createMutation = useCreateHotel()

  return (
    <div className="space-y-6">
      <Link href="/provider/hotels" className="inline-flex items-center text-sm text-muted-foreground hover:text-foreground">
        <ArrowLeft className="h-4 w-4 mr-1" />
        Back to Hotels
      </Link>

      <div>
        <h1 className="text-2xl font-bold">Add New Hotel</h1>
        <p className="text-muted-foreground">Fill in the details of your property</p>
      </div>

      <div className="rounded-xl border bg-card p-6">
        <HotelForm
          isPending={createMutation.isPending}
          onSubmit={(data) => {
            createMutation.mutate(data, {
              onSuccess: () => router.push('/provider/hotels'),
            })
          }}
        />
      </div>
    </div>
  )
}

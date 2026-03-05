'use client'

import { useState } from 'react'
import { useQueries } from '@tanstack/react-query'
import { useMyHotels } from '@/hooks/use-hotels'
import { useMyGuideProfile } from '@/hooks/use-guides'
import { useMyVehicles } from '@/hooks/use-vehicles'
import { useReviewsByEntity } from '@/hooks/use-reviews'
import { useUserRole } from '@/hooks/use-user-role'
import { StarRating } from '@/components/shared/StarRating'
import { Pagination } from '@/components/shared/Pagination'
import { EmptyState } from '@/components/shared/EmptyState'
import { ResponseDialog } from '@/components/provider/ResponseDialog'
import { Star } from 'lucide-react'
import { formatDate, getProviderTypeLabel } from '@/lib/utils'
import { Badge } from '@/components/ui/badge'
import { getReviewsByEntity } from '@/lib/api/review'
import type { Review } from '@/types/review'

export default function ProviderReviewsPage() {
  const { role } = useUserRole()
  const [page, setPage] = useState(0)

  const { data: hotelsData } = useMyHotels()
  const { data: guideData } = useMyGuideProfile({ enabled: role === 'TOUR_GUIDE' })
  const { data: vehiclesData } = useMyVehicles()

  const vehicles = vehiclesData ?? []
  const firstHotelId = hotelsData?.data?.[0]?.id
  const guideId = guideData?.data?.id

  // For VEHICLE_OWNER: fetch reviews for every vehicle in parallel
  const vehicleReviewQueries = useQueries({
    queries: role === 'VEHICLE_OWNER'
      ? vehicles.map((v: any) => ({
          queryKey: ['reviews', 'VEHICLE', v.id, { page: 0, size: 100 }],
          queryFn: () => getReviewsByEntity('VEHICLE', v.id, { page: 0, size: 100 }),
          enabled: true,
        }))
      : [],
  })

  const entityType = role === 'HOTEL_OWNER' ? 'HOTEL' : 'TOUR_GUIDE'
  const entityId = role === 'HOTEL_OWNER' ? firstHotelId : guideId

  // For HOTEL_OWNER / TOUR_GUIDE use the existing single-entity hook with pagination
  const { data: singleData, isLoading: singleLoading } = useReviewsByEntity(
    entityType,
    entityId ?? 0,
    { page, size: 10 },
  )

  // Merge and sort all vehicle reviews by date desc
  const allVehicleReviews: Review[] = vehicleReviewQueries
    .flatMap((q) => (q.data?.data?.data ?? []) as Review[])
    .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())

  const vehicleReviewsLoading = vehicleReviewQueries.some((q) => q.isLoading)

  const isLoading = role === 'VEHICLE_OWNER' ? vehicleReviewsLoading : singleLoading
  const reviews: Review[] = role === 'VEHICLE_OWNER' ? allVehicleReviews : (singleData?.data?.data ?? [])
  const pagination = role === 'VEHICLE_OWNER' ? undefined : singleData?.data?.pagination
  const hasProvider = role === 'VEHICLE_OWNER' ? vehicles.length > 0 : !!entityId

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Reviews</h1>
        <p className="text-muted-foreground">Reviews from tourists for your {role === 'HOTEL_OWNER' ? 'hotels' : role === 'VEHICLE_OWNER' ? 'vehicles' : 'guide services'}</p>
      </div>

      {isLoading ? (
        <div className="space-y-3">
          {[1, 2, 3].map((i) => (
            <div key={i} className="rounded-lg border bg-card p-4 animate-pulse">
              <div className="h-4 bg-muted rounded w-32 mb-2" />
              <div className="h-4 bg-muted rounded w-3/4" />
            </div>
          ))}
        </div>
      ) : !hasProvider ? (
        <EmptyState
          icon={Star}
          title="No provider profile"
          description="Set up your provider profile first to see reviews."
        />
      ) : reviews.length === 0 ? (
        <EmptyState
          icon={Star}
          title="No reviews yet"
          description="Reviews from tourists will appear here."
        />
      ) : (
        <>
          <div className="space-y-3">
            {reviews.map((review) => (
              <div key={review.id} className="rounded-lg border bg-card p-4">
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <div className="flex items-center gap-2 mb-1">
                      <StarRating rating={review.rating} size="sm" />
                      <Badge variant="outline" className="text-[10px]">
                        {getProviderTypeLabel(review.entityType)}
                      </Badge>
                      {review.isVerified && (
                        <Badge className="bg-green-100 text-green-700 text-[10px]">Verified</Badge>
                      )}
                    </div>
                    {review.title && <p className="font-medium text-sm">{review.title}</p>}
                  </div>
                  {review.responses.length === 0 && (
                    <ResponseDialog reviewId={review.id} />
                  )}
                </div>
                {review.content && <p className="text-sm text-muted-foreground mt-2">{review.content}</p>}
                <p className="text-xs text-muted-foreground mt-2">{formatDate(review.createdAt)}</p>

                {review.responses.length > 0 && (
                  <div className="mt-3 ml-4 pl-4 border-l-2 border-primary/20 space-y-2">
                    {review.responses.map((resp) => (
                      <div key={resp.id}>
                        <p className="font-medium text-primary text-xs">Your Response</p>
                        <p className="text-sm text-muted-foreground">{resp.content}</p>
                        <p className="text-xs text-muted-foreground mt-0.5">{formatDate(resp.createdAt)}</p>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            ))}
          </div>
          {pagination && (
            <Pagination page={page} totalPages={pagination.totalPages} onPageChange={setPage} />
          )}
        </>
      )}
    </div>
  )
}

'use client'

import { useState } from 'react'
import { useMyHotels } from '@/hooks/use-hotels'
import { useMyGuideProfile } from '@/hooks/use-guides'
import { useReviewsByEntity } from '@/hooks/use-reviews'
import { useUserRole } from '@/hooks/use-user-role'
import { StarRating } from '@/components/shared/StarRating'
import { Pagination } from '@/components/shared/Pagination'
import { EmptyState } from '@/components/shared/EmptyState'
import { ResponseDialog } from '@/components/provider/ResponseDialog'
import { Star } from 'lucide-react'
import { formatDate, getProviderTypeLabel } from '@/lib/utils'
import { Badge } from '@/components/ui/badge'
import type { Review } from '@/types/review'

export default function ProviderReviewsPage() {
  const { role } = useUserRole()
  const [page, setPage] = useState(0)

  const { data: hotelsData } = useMyHotels()
  const { data: guideData } = useMyGuideProfile()

  const firstHotelId = hotelsData?.data?.[0]?.id
  const guideId = guideData?.data?.id

  const entityType = role === 'HOTEL_OWNER' ? 'HOTEL' : 'TOUR_GUIDE'
  const entityId = role === 'HOTEL_OWNER' ? firstHotelId : guideId

  const { data, isLoading } = useReviewsByEntity(entityType, entityId ?? 0, { page, size: 10 })

  const reviews: Review[] = data?.data?.data ?? []
  const pagination = data?.data?.pagination

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Reviews</h1>
        <p className="text-muted-foreground">Reviews from tourists for your {role === 'HOTEL_OWNER' ? 'hotels' : 'guide services'}</p>
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
      ) : !entityId ? (
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

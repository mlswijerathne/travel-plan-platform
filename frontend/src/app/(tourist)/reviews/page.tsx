'use client'

import { useState } from 'react'
import { useMyReviews } from '@/hooks/use-reviews'
import { ReviewCard } from '@/components/reviews/ReviewCard'
import { Pagination } from '@/components/shared/Pagination'
import { EmptyState } from '@/components/shared/EmptyState'
import { Star } from 'lucide-react'

export default function ReviewsPage() {
  const [page, setPage] = useState(0)
  const { data, isLoading } = useMyReviews({ page, size: 10 })

  const reviews = data?.data?.data ?? []
  const pagination = data?.data?.pagination

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Reviews</h1>
        <p className="text-muted-foreground">Manage your reviews and write new ones</p>
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
      ) : reviews.length === 0 ? (
        <EmptyState
          icon={Star}
          title="No reviews yet"
          description="Your reviews will appear here after you write them. You can write reviews from completed bookings."
        />
      ) : (
        <>
          <div className="space-y-3">
            {reviews.map((review) => (
              <ReviewCard key={review.id} review={review} editable />
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

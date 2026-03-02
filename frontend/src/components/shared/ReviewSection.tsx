'use client'

import { useState } from 'react'
import { Star } from 'lucide-react'
import { useReviewsByEntity, useReviewSummary } from '@/hooks/use-reviews'
import { StarRating } from './StarRating'
import { Pagination } from './Pagination'
import { EmptyState } from './EmptyState'
import { formatDate } from '@/lib/utils'
import type { Review } from '@/types/review'

interface ReviewSectionProps {
  entityType: string
  entityId: number
}

function ReviewItem({ review }: { review: Review }) {
  return (
    <div className="border-b last:border-0 py-4">
      <div className="flex items-center gap-3 mb-2">
        <div className="h-8 w-8 rounded-full bg-primary/10 flex items-center justify-center text-xs font-semibold text-primary">
          {review.touristId.slice(0, 2).toUpperCase()}
        </div>
        <div className="flex-1">
          <div className="flex items-center gap-2">
            <StarRating rating={review.rating} size="sm" />
            {review.isVerified && (
              <span className="text-[10px] bg-green-100 text-green-700 px-1.5 py-0.5 rounded-full font-medium">
                Verified
              </span>
            )}
          </div>
          <p className="text-xs text-muted-foreground">{formatDate(review.createdAt)}</p>
        </div>
      </div>
      {review.title && <p className="font-medium text-sm mb-1">{review.title}</p>}
      {review.content && <p className="text-sm text-muted-foreground">{review.content}</p>}
      {review.responses.length > 0 && (
        <div className="mt-3 ml-6 pl-4 border-l-2 border-primary/20">
          {review.responses.map((resp) => (
            <div key={resp.id} className="text-sm">
              <p className="font-medium text-primary text-xs mb-0.5">Provider Response</p>
              <p className="text-muted-foreground">{resp.content}</p>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

function RatingSummaryBar({ label, count, total }: { label: string; count: number; total: number }) {
  const pct = total > 0 ? (count / total) * 100 : 0
  return (
    <div className="flex items-center gap-2 text-sm">
      <span className="w-3 text-muted-foreground">{label}</span>
      <Star className="h-3 w-3 fill-amber-400 text-amber-400" />
      <div className="flex-1 h-2 bg-muted rounded-full overflow-hidden">
        <div className="h-full bg-amber-400 rounded-full" style={{ width: `${pct}%` }} />
      </div>
      <span className="w-6 text-right text-muted-foreground text-xs">{count}</span>
    </div>
  )
}

export function ReviewSection({ entityType, entityId }: ReviewSectionProps) {
  const [page, setPage] = useState(0)
  const { data: reviewsData, isLoading } = useReviewsByEntity(entityType, entityId, { page, size: 5 })
  const { data: summaryData } = useReviewSummary(entityType, entityId)

  const reviews = reviewsData?.data?.data ?? []
  const pagination = reviewsData?.data?.pagination
  const summary = summaryData?.data

  return (
    <div className="space-y-6">
      <h3 className="text-lg font-semibold">Reviews</h3>

      {summary && summary.reviewCount > 0 && (
        <div className="flex flex-col sm:flex-row gap-6 p-4 rounded-lg bg-muted/30">
          <div className="text-center sm:text-left">
            <p className="text-4xl font-bold">{summary.averageRating.toFixed(1)}</p>
            <StarRating rating={Math.round(summary.averageRating)} size="sm" />
            <p className="text-sm text-muted-foreground mt-1">{summary.reviewCount} reviews</p>
          </div>
          <div className="flex-1 space-y-1.5">
            <RatingSummaryBar label="5" count={summary.fiveStarCount} total={summary.reviewCount} />
            <RatingSummaryBar label="4" count={summary.fourStarCount} total={summary.reviewCount} />
            <RatingSummaryBar label="3" count={summary.threeStarCount} total={summary.reviewCount} />
            <RatingSummaryBar label="2" count={summary.twoStarCount} total={summary.reviewCount} />
            <RatingSummaryBar label="1" count={summary.oneStarCount} total={summary.reviewCount} />
          </div>
        </div>
      )}

      {isLoading ? (
        <div className="space-y-4">
          {[1, 2, 3].map((i) => (
            <div key={i} className="animate-pulse border-b pb-4">
              <div className="flex items-center gap-3 mb-2">
                <div className="h-8 w-8 rounded-full bg-muted" />
                <div className="h-4 bg-muted rounded w-24" />
              </div>
              <div className="h-4 bg-muted rounded w-3/4" />
            </div>
          ))}
        </div>
      ) : reviews.length === 0 ? (
        <EmptyState icon={Star} title="No reviews yet" description="Be the first to leave a review!" />
      ) : (
        <>
          <div>
            {reviews.map((review: Review) => (
              <ReviewItem key={review.id} review={review} />
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

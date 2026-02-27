'use client'

import { useState } from 'react'
import { useMyReviews, usePendingReviews } from '@/hooks/use-reviews'
import { ReviewCard } from '@/components/reviews/ReviewCard'
import { PendingReviewCard } from '@/components/reviews/PendingReviewCard'
import { Pagination } from '@/components/shared/Pagination'
import { EmptyState } from '@/components/shared/EmptyState'
import { Button } from '@/components/ui/button'
import { Star, Clock } from 'lucide-react'

type Tab = 'my' | 'pending'

export default function ReviewsPage() {
  const [tab, setTab] = useState<Tab>('my')
  const [myPage, setMyPage] = useState(0)
  const [pendingPage, setPendingPage] = useState(0)

  const { data: myData, isLoading: myLoading } = useMyReviews({ page: myPage, size: 10 })
  const { data: pendingData, isLoading: pendingLoading } = usePendingReviews({ page: pendingPage, size: 10 })

  const myReviews = myData?.data?.data ?? []
  const myPagination = myData?.data?.pagination
  const pendingReviews = pendingData?.data?.data ?? []
  const pendingPagination = pendingData?.data?.pagination

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Reviews</h1>
        <p className="text-muted-foreground">Manage your reviews and write new ones</p>
      </div>

      <div className="flex gap-1.5">
        <Button
          variant={tab === 'my' ? 'default' : 'outline'}
          size="sm"
          onClick={() => setTab('my')}
        >
          <Star className="h-3.5 w-3.5 mr-1" />
          My Reviews
        </Button>
        <Button
          variant={tab === 'pending' ? 'default' : 'outline'}
          size="sm"
          onClick={() => setTab('pending')}
        >
          <Clock className="h-3.5 w-3.5 mr-1" />
          Pending
          {pendingReviews.length > 0 && (
            <span className="ml-1 h-5 w-5 rounded-full bg-primary/20 text-[10px] flex items-center justify-center">
              {pendingPagination?.totalItems ?? pendingReviews.length}
            </span>
          )}
        </Button>
      </div>

      {tab === 'my' && (
        <>
          {myLoading ? (
            <div className="space-y-3">
              {[1, 2, 3].map((i) => (
                <div key={i} className="rounded-lg border bg-card p-4 animate-pulse">
                  <div className="h-4 bg-muted rounded w-32 mb-2" />
                  <div className="h-4 bg-muted rounded w-3/4" />
                </div>
              ))}
            </div>
          ) : myReviews.length === 0 ? (
            <EmptyState
              icon={Star}
              title="No reviews yet"
              description="Your reviews will appear here after you write them."
            />
          ) : (
            <>
              <div className="space-y-3">
                {myReviews.map((review) => (
                  <ReviewCard key={review.id} review={review} editable />
                ))}
              </div>
              {myPagination && (
                <Pagination page={myPage} totalPages={myPagination.totalPages} onPageChange={setMyPage} />
              )}
            </>
          )}
        </>
      )}

      {tab === 'pending' && (
        <>
          {pendingLoading ? (
            <div className="space-y-3">
              {[1, 2].map((i) => (
                <div key={i} className="rounded-lg border bg-card p-4 animate-pulse">
                  <div className="h-5 bg-muted rounded w-48" />
                </div>
              ))}
            </div>
          ) : pendingReviews.length === 0 ? (
            <EmptyState
              icon={Clock}
              title="No pending reviews"
              description="When you complete a booking, you'll be able to review it here."
            />
          ) : (
            <>
              <div className="space-y-3">
                {pendingReviews.map((pending) => (
                  <PendingReviewCard key={pending.id} pending={pending} />
                ))}
              </div>
              {pendingPagination && (
                <Pagination page={pendingPage} totalPages={pendingPagination.totalPages} onPageChange={setPendingPage} />
              )}
            </>
          )}
        </>
      )}
    </div>
  )
}

'use client'

import { useState } from 'react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import type { PendingReview } from '@/types/review'
import { formatDate, getProviderTypeLabel } from '@/lib/utils'
import { ReviewFormDialog } from './ReviewFormDialog'
import { Star } from 'lucide-react'

export function PendingReviewCard({ pending }: { pending: PendingReview }) {
  const [dialogOpen, setDialogOpen] = useState(false)

  return (
    <div className="rounded-lg border bg-card p-4 flex items-center gap-4">
      <div className="h-10 w-10 rounded-lg bg-amber-50 flex items-center justify-center shrink-0">
        <Star className="h-5 w-5 text-amber-500" />
      </div>
      <div className="flex-1 min-w-0">
        <p className="font-medium text-sm truncate">{pending.entityName}</p>
        <div className="flex items-center gap-2 mt-0.5">
          <Badge variant="outline" className="text-[10px]">{getProviderTypeLabel(pending.entityType)}</Badge>
          <span className="text-xs text-muted-foreground">Trip ended {formatDate(pending.tripEndDate)}</span>
        </div>
      </div>
      <Button size="sm" onClick={() => setDialogOpen(true)}>Write Review</Button>

      <ReviewFormDialog
        open={dialogOpen}
        onOpenChange={setDialogOpen}
        entityType={pending.entityType}
        entityId={pending.entityId}
        bookingId={pending.bookingId}
        entityName={pending.entityName}
      />
    </div>
  )
}

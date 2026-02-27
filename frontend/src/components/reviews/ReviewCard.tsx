'use client'

import { useState } from 'react'
import { StarRating } from '@/components/shared/StarRating'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Pencil, Trash2 } from 'lucide-react'
import type { Review } from '@/types/review'
import { formatDate, getProviderTypeLabel } from '@/lib/utils'
import { useDeleteReview } from '@/hooks/use-reviews'
import { ReviewFormDialog } from './ReviewFormDialog'

export function ReviewCard({ review, editable = false }: { review: Review; editable?: boolean }) {
  const [editing, setEditing] = useState(false)
  const deleteMutation = useDeleteReview()

  return (
    <div className="rounded-lg border bg-card p-4">
      <div className="flex items-start justify-between gap-2">
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
        {editable && (
          <div className="flex gap-1 shrink-0">
            <Button variant="ghost" size="sm" className="h-8 w-8 p-0" onClick={() => setEditing(true)}>
              <Pencil className="h-3.5 w-3.5" />
            </Button>
            <Button
              variant="ghost"
              size="sm"
              className="h-8 w-8 p-0 text-destructive hover:text-destructive"
              onClick={() => deleteMutation.mutate(review.id)}
              disabled={deleteMutation.isPending}
            >
              <Trash2 className="h-3.5 w-3.5" />
            </Button>
          </div>
        )}
      </div>

      {review.content && (
        <p className="text-sm text-muted-foreground mt-2">{review.content}</p>
      )}
      <p className="text-xs text-muted-foreground mt-2">{formatDate(review.createdAt)}</p>

      {review.responses.length > 0 && (
        <div className="mt-3 ml-4 pl-4 border-l-2 border-primary/20 space-y-2">
          {review.responses.map((resp) => (
            <div key={resp.id}>
              <p className="font-medium text-primary text-xs">Provider Response</p>
              <p className="text-sm text-muted-foreground">{resp.content}</p>
              <p className="text-xs text-muted-foreground mt-0.5">{formatDate(resp.createdAt)}</p>
            </div>
          ))}
        </div>
      )}

      {editing && (
        <ReviewFormDialog
          open={editing}
          onOpenChange={setEditing}
          editReview={review}
        />
      )}
    </div>
  )
}

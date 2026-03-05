'use client'

import { useState, useEffect } from 'react'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { StarRating } from '@/components/shared/StarRating'
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { useCreateReview, useUpdateReview } from '@/hooks/use-reviews'
import type { Review, EntityType } from '@/types/review'

interface ReviewFormDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  editReview?: Review
  entityType?: EntityType
  entityId?: number
  bookingId?: number
  entityName?: string
}

export function ReviewFormDialog({
  open,
  onOpenChange,
  editReview,
  entityType,
  entityId,
  bookingId,
  entityName,
}: ReviewFormDialogProps) {
  const [rating, setRating] = useState(editReview?.rating ?? 0)
  const [title, setTitle] = useState(editReview?.title ?? '')
  const [content, setContent] = useState(editReview?.content ?? '')
  const [errorMsg, setErrorMsg] = useState<string | null>(null)

  // Reset form state whenever the dialog opens
  useEffect(() => {
    if (open) {
      setRating(editReview?.rating ?? 0)
      setTitle(editReview?.title ?? '')
      setContent(editReview?.content ?? '')
      setErrorMsg(null)
    }
  }, [open, editReview])

  const createMutation = useCreateReview()
  const updateMutation = useUpdateReview()
  const isPending = createMutation.isPending || updateMutation.isPending

  const isEditing = !!editReview

  function handleSuccess() {
    toast.success(isEditing ? 'Review updated!' : 'Review submitted — thank you!')
    onOpenChange(false)
  }

  function handleError(err: Error) {
    const msg = err.message || 'Something went wrong. Please try again.'
    setErrorMsg(msg)
    toast.error(msg)
  }

  function handleSubmit() {
    if (rating === 0) return
    setErrorMsg(null)

    if (isEditing) {
      updateMutation.mutate(
        { id: editReview.id, data: { rating, title: title || undefined, content: content || undefined } },
        { onSuccess: handleSuccess, onError: handleError }
      )
    } else if (entityType && entityId) {
      createMutation.mutate(
        {
          entityType,
          entityId,
          bookingId,
          rating,
          title: title || undefined,
          content: content || undefined,
        },
        { onSuccess: handleSuccess, onError: handleError }
      )
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>
            {isEditing ? 'Edit Review' : `Review ${entityName ?? ''}`}
          </DialogTitle>
        </DialogHeader>

        <div className="space-y-4">
          <div>
            <label className="text-sm text-muted-foreground mb-1 block">Rating</label>
            <StarRating rating={rating} interactive onChange={setRating} size="lg" />
            {rating === 0 && <p className="text-xs text-muted-foreground mt-1">Please select a star rating</p>}
          </div>
          <div>
            <label className="text-sm text-muted-foreground mb-1 block">Title (optional)</label>
            <Input value={title} onChange={(e) => setTitle(e.target.value)} placeholder="Brief summary" />
          </div>
          <div>
            <label className="text-sm text-muted-foreground mb-1 block">Review (optional)</label>
            <Textarea
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder="Share your experience..."
              rows={4}
            />
          </div>
          {errorMsg && (
            <p className="text-sm text-destructive bg-destructive/10 rounded-md px-3 py-2">{errorMsg}</p>
          )}
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Cancel</Button>
          <Button onClick={handleSubmit} disabled={isPending || rating === 0}>
            {isPending ? 'Saving...' : isEditing ? 'Update' : 'Submit'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}


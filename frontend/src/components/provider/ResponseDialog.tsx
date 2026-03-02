'use client'

import { useState } from 'react'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import { useAddProviderResponse } from '@/hooks/use-reviews'
import { MessageSquare } from 'lucide-react'

interface ResponseDialogProps {
  reviewId: number
}

export function ResponseDialog({ reviewId }: ResponseDialogProps) {
  const [open, setOpen] = useState(false)
  const [content, setContent] = useState('')
  const mutation = useAddProviderResponse()

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button variant="outline" size="sm">
          <MessageSquare className="h-3.5 w-3.5 mr-1" />
          Respond
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Respond to Review</DialogTitle>
        </DialogHeader>
        <Textarea
          value={content}
          onChange={(e) => setContent(e.target.value)}
          placeholder="Write your response..."
          rows={4}
        />
        <DialogFooter>
          <Button variant="outline" onClick={() => setOpen(false)}>Cancel</Button>
          <Button
            disabled={!content.trim() || mutation.isPending}
            onClick={() => {
              mutation.mutate(
                { reviewId, data: { content: content.trim() } },
                { onSuccess: () => { setOpen(false); setContent('') } }
              )
            }}
          >
            {mutation.isPending ? 'Sending...' : 'Send Response'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

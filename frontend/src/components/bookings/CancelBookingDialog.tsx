'use client'

import { useState } from 'react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'

interface CancelBookingDialogProps {
  onConfirm: (reason: string) => void
  isPending: boolean
}

export function CancelBookingDialog({ onConfirm, isPending }: CancelBookingDialogProps) {
  const [open, setOpen] = useState(false)
  const [reason, setReason] = useState('')

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button variant="destructive" size="sm">Cancel Booking</Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Cancel Booking</DialogTitle>
          <DialogDescription>
            Are you sure you want to cancel this booking? This action may affect your refund amount depending on the cancellation policy.
          </DialogDescription>
        </DialogHeader>
        <div>
          <label className="text-sm text-muted-foreground mb-1 block">Reason (optional)</label>
          <Input
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            placeholder="Why are you cancelling?"
          />
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => setOpen(false)}>Keep Booking</Button>
          <Button
            variant="destructive"
            disabled={isPending}
            onClick={() => {
              onConfirm(reason)
              setOpen(false)
            }}
          >
            {isPending ? 'Cancelling...' : 'Confirm Cancel'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

'use client'

import type { QuickReplyChip } from '@/types/chat'
import { ChevronRight } from 'lucide-react'

interface QuickRepliesProps {
  chips: QuickReplyChip[]
  onSelect: (value: string) => void
}

export function QuickReplies({ chips, onSelect }: QuickRepliesProps) {
  return (
    <div className="flex flex-wrap gap-2">
      {chips.map((chip) => (
        <button
          key={chip.value}
          className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-full text-xs font-medium border border-primary/20 bg-primary/5 text-primary hover:bg-primary hover:text-white transition-all duration-200 hover:shadow-sm"
          onClick={() => onSelect(chip.value)}
        >
          {chip.label}
          <ChevronRight className="h-3 w-3" />
        </button>
      ))}
    </div>
  )
}

'use client'

import { cn } from '@/lib/utils'
import type { ChatMessage as ChatMessageType } from '@/types/chat'
import { QuickReplies } from './QuickReplies'
import { MarkdownContent } from './MarkdownContent'
import { RecommendationGroup } from './RecommendationGroup'
import { ProviderCard } from './ProviderCard'
import { Avatar, AvatarFallback } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { Compass, Loader2 } from 'lucide-react'

interface ChatMessageProps {
  message: ChatMessageType
  onQuickReply?: (value: string) => void
  onAddToBooking?: (provider: import('@/types/chat').ProviderResult) => void
  isLast?: boolean
}

export function ChatMessage({ message, onQuickReply, onAddToBooking, isLast }: ChatMessageProps) {
  const isUser = message.role === 'user'
  const hasMultipleProviderTypes = message.providers
    ? new Set(message.providers.map(p => p.type)).size > 1
    : false

  return (
    <div className={cn('flex gap-3 max-w-4xl', isUser ? 'ml-auto flex-row-reverse' : '')}>
      {/* Avatar */}
      <Avatar className={cn('h-8 w-8 shrink-0 mt-1', isUser ? 'ring-2 ring-primary/20' : 'ring-2 ring-border/50')}>
        <AvatarFallback className={cn(
          'text-xs font-semibold',
          isUser
            ? 'bg-primary text-white'
            : 'bg-gradient-to-br from-teal-50 to-teal-100 text-primary'
        )}>
          {isUser ? 'You' : <Compass className="h-4 w-4" />}
        </AvatarFallback>
      </Avatar>

      <div className={cn('space-y-3 min-w-0 flex flex-col', isUser ? 'items-end' : 'items-start')}>
        {/* Tool call indicator */}
        {message.toolName && (
          <Badge variant="secondary" className="text-xs font-mono gap-1.5 animate-pulse">
            <Loader2 className="h-3 w-3 animate-spin" />
            {message.toolName}
          </Badge>
        )}

        {/* Message bubble */}
        {message.content && (
          <div className={cn(
            'rounded-2xl px-4 py-3 max-w-prose',
            isUser
              ? 'bg-primary text-white rounded-tr-sm'
              : 'bg-white border border-border/50 shadow-sm rounded-tl-sm'
          )}>
            {isUser ? (
              <p className="text-sm leading-relaxed whitespace-pre-wrap">{message.content}</p>
            ) : (
              <MarkdownContent content={message.content} />
            )}
            {message.isStreaming && (
              <span className="inline-block w-1.5 h-4 bg-primary ml-0.5 animate-pulse rounded-full" />
            )}
          </div>
        )}

        {/* Provider results */}
        {message.providers && message.providers.length > 0 && (
          hasMultipleProviderTypes ? (
            <RecommendationGroup providers={message.providers} onAddToBooking={onAddToBooking} />
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 w-full max-w-2xl">
              {message.providers.map((provider) => (
                <ProviderCard key={provider.id} provider={provider} onAddToBooking={onAddToBooking} />
              ))}
            </div>
          )
        )}

        {/* Quick replies */}
        {isLast && message.quickReplies && message.quickReplies.length > 0 && onQuickReply && (
          <QuickReplies chips={message.quickReplies} onSelect={onQuickReply} />
        )}
      </div>
    </div>
  )
}

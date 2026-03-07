'use client'

import { useEffect, useState } from 'react'
import { getChatSessions, deleteChatSession, type ChatSession } from '@/lib/api/chat'
import { Button } from '@/components/ui/button'
import { ScrollArea } from '@/components/ui/scroll-area'
import { cn } from '@/lib/utils'
import { MessageSquare, Trash2, Loader2, X } from 'lucide-react'
import { toast } from 'sonner'

interface ChatHistorySidebarProps {
  open: boolean
  onClose: () => void
  currentSessionId: string | null
  onSelectSession: (sessionId: string) => void
}

function formatRelativeTime(dateStr: string): string {
  const date = new Date(dateStr)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMins / 60)
  const diffDays = Math.floor(diffHours / 24)

  if (diffMins < 1) return 'Just now'
  if (diffMins < 60) return `${diffMins}m ago`
  if (diffHours < 24) return `${diffHours}h ago`
  if (diffDays < 7) return `${diffDays}d ago`
  return date.toLocaleDateString()
}

export function ChatHistorySidebar({ open, onClose, currentSessionId, onSelectSession }: ChatHistorySidebarProps) {
  const [sessions, setSessions] = useState<ChatSession[]>([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (open) {
      loadSessions()
    }
  }, [open])

  async function loadSessions() {
    setLoading(true)
    try {
      const data = await getChatSessions()
      setSessions(data)
    } catch {
      toast.error('Failed to load chat history')
    } finally {
      setLoading(false)
    }
  }

  async function handleDelete(e: React.MouseEvent, sessionId: string) {
    e.stopPropagation()
    try {
      await deleteChatSession(sessionId)
      setSessions(prev => prev.filter(s => s.sessionId !== sessionId))
      toast.success('Chat deleted')
    } catch {
      toast.error('Failed to delete chat')
    }
  }

  if (!open) return null

  return (
    <div className="w-72 border-r border-border/50 bg-muted/30 flex flex-col h-full">
      <div className="flex items-center justify-between px-4 py-3 border-b border-border/50">
        <h3 className="text-sm font-semibold">Chat History</h3>
        <Button variant="ghost" size="sm" className="h-7 w-7 p-0" onClick={onClose}>
          <X className="h-4 w-4" />
        </Button>
      </div>

      <ScrollArea className="flex-1">
        {loading ? (
          <div className="flex items-center justify-center py-8">
            <Loader2 className="h-5 w-5 animate-spin text-muted-foreground" />
          </div>
        ) : sessions.length === 0 ? (
          <p className="text-xs text-muted-foreground text-center py-8 px-4">
            No previous chats yet
          </p>
        ) : (
          <div className="p-2 space-y-1">
            {sessions.map(session => (
              <button
                key={session.sessionId}
                onClick={() => onSelectSession(session.sessionId)}
                className={cn(
                  'w-full text-left rounded-lg px-3 py-2.5 text-sm transition-colors group',
                  'hover:bg-muted',
                  session.sessionId === currentSessionId && 'bg-primary/10 border border-primary/20'
                )}
              >
                <div className="flex items-start gap-2">
                  <MessageSquare className="h-4 w-4 text-muted-foreground shrink-0 mt-0.5" />
                  <div className="flex-1 min-w-0">
                    <p className="text-xs font-medium truncate">
                      {session.title || 'New Chat'}
                    </p>
                    <p className="text-[10px] text-muted-foreground mt-0.5">
                      {formatRelativeTime(session.lastActivityAt)}
                    </p>
                  </div>
                  <Button
                    variant="ghost"
                    size="sm"
                    className="h-6 w-6 p-0 opacity-0 group-hover:opacity-100 transition-opacity text-muted-foreground hover:text-destructive"
                    onClick={(e) => handleDelete(e, session.sessionId)}
                  >
                    <Trash2 className="h-3 w-3" />
                  </Button>
                </div>
              </button>
            ))}
          </div>
        )}
      </ScrollArea>
    </div>
  )
}

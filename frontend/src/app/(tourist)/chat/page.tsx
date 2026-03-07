'use client'

import { useCallback, useEffect, useRef, useState } from 'react'
import { streamChat, streamGeneratePlan, getChatHistory } from '@/lib/api/chat'
import type { ChatMessage as ChatMessageType, ChatStreamEvent, ProviderResult } from '@/types/chat'
import type { TripPlanFormData } from '@/types/trip-plan'
import { ChatMessage } from '@/components/chat/ChatMessage'
import { AgentStatusBar } from '@/components/chat/AgentStatusBar'
import { TripPlanDialog } from '@/components/chat/TripPlanDialog'
import { BookingCart } from '@/components/chat/BookingCart'
import { ChatHistorySidebar } from '@/components/chat/ChatHistorySidebar'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Badge } from '@/components/ui/badge'
import { toast } from 'sonner'
import { Send, Plus, RotateCcw, Compass, Palmtree, Mountain, UtensilsCrossed, Camera, ShoppingCart, History } from 'lucide-react'

const WELCOME_MESSAGE: ChatMessageType = {
  id: 'welcome',
  role: 'assistant',
  content: "Hello! I'm your AI travel companion for Sri Lanka. Tell me about your dream trip — where you'd like to go, how many days, your budget, and what interests you. I'll create a personalized itinerary with the best hotels, guides, and transport options!",
  timestamp: new Date(),
  quickReplies: [
    { label: '3 days in Colombo', value: 'I want to visit Colombo for 3 days' },
    { label: 'Beach vacation', value: 'I want a relaxing beach vacation for a week' },
    { label: 'Cultural tour', value: 'Plan a cultural heritage tour of Sri Lanka' },
    { label: 'Adventure trip', value: 'I want an adventure trip with hiking and safari' },
  ],
}

const QUICK_START_CHIPS = [
  { icon: Palmtree, label: 'Beach Getaway', value: 'I want a relaxing beach vacation in Sri Lanka' },
  { icon: Mountain, label: 'Adventure', value: 'Plan an adventure trip with hiking and safari in Sri Lanka' },
  { icon: Camera, label: 'Cultural Tour', value: 'Plan a cultural heritage tour of Sri Lanka' },
  { icon: UtensilsCrossed, label: 'Food Journey', value: 'I want a food and culinary tour of Sri Lanka' },
]

export default function ChatPage() {
  const [messages, setMessages] = useState<ChatMessageType[]>([WELCOME_MESSAGE])
  const [input, setInput] = useState('')
  const [isStreaming, setIsStreaming] = useState(false)
  const [sessionId, setSessionId] = useState<string | null>(null)
  const [activeAgent, setActiveAgent] = useState<string | null>(null)
  const [activeTool, setActiveTool] = useState<string | null>(null)
  const [planDialogOpen, setPlanDialogOpen] = useState(false)
  const [bookingItems, setBookingItems] = useState<ProviderResult[]>([])
  const [cartOpen, setCartOpen] = useState(false)
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const scrollRef = useRef<HTMLDivElement>(null)
  const textareaRef = useRef<HTMLTextAreaElement>(null)

  function handleAddToBooking(provider: ProviderResult) {
    setBookingItems(prev => {
      if (prev.some(item => item.id === provider.id && item.type === provider.type)) {
        toast.info(`${provider.name} is already in your booking`)
        return prev
      }
      toast.success(`${provider.name} added to booking`)
      return [...prev, provider]
    })
  }

  function handleRemoveFromBooking(providerId: string) {
    setBookingItems(prev => prev.filter(item => item.id !== providerId))
  }

  function handleClearBooking() {
    setBookingItems([])
  }

  async function handleSelectSession(selectedSessionId: string) {
    if (selectedSessionId === sessionId) {
      setSidebarOpen(false)
      return
    }
    try {
      const response = await getChatHistory(selectedSessionId)
      const history = response.data
      if (history?.messages?.length > 0) {
        const loadedMessages: ChatMessageType[] = history.messages.map(
          (msg: { role: string; content: string; timestamp: string; quickReplies?: unknown[]; providers?: ProviderResult[] }, i: number) => ({
            id: `loaded-${i}`,
            role: msg.role as 'user' | 'assistant',
            content: msg.content,
            timestamp: new Date(msg.timestamp),
            quickReplies: msg.quickReplies,
            providers: msg.providers,
          })
        )
        setMessages(loadedMessages)
        setSessionId(selectedSessionId)
      }
    } catch {
      toast.error('Failed to load chat session')
    }
    setSidebarOpen(false)
  }

  const scrollToBottom = useCallback(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight
    }
  }, [])

  useEffect(() => {
    scrollToBottom()
  }, [messages, scrollToBottom])

  function handleStreamEvents(
    assistantId: string,
    onEvent: (event: ChatStreamEvent) => void = () => {},
  ) {
    return (event: ChatStreamEvent) => {
      switch (event.type) {
        case 'TEXT_DELTA':
          setMessages(prev =>
            prev.map(m =>
              m.id === assistantId
                ? { ...m, content: m.content + (event.content ?? '') }
                : m
            )
          )
          break

        case 'TOOL_CALL':
          setActiveTool(event.toolName ?? null)
          setMessages(prev =>
            prev.map(m =>
              m.id === assistantId
                ? { ...m, toolName: event.toolName }
                : m
            )
          )
          break

        case 'AGENT_TRANSFER':
          setActiveAgent(event.agentName ?? null)
          break

        case 'FINAL_RESPONSE':
          setMessages(prev =>
            prev.map(m =>
              m.id === assistantId
                ? {
                    ...m,
                    content: event.content ?? m.content,
                    quickReplies: event.quickReplies,
                    providers: event.providers,
                    isStreaming: false,
                    toolName: undefined,
                  }
                : m
            )
          )
          if (event.sessionId) setSessionId(event.sessionId)
          break

        case 'DONE':
          if (event.sessionId) setSessionId(event.sessionId)
          setMessages(prev =>
            prev.map(m =>
              m.id === assistantId ? { ...m, isStreaming: false, toolName: undefined } : m
            )
          )
          break

        case 'ERROR':
          toast.error(event.content ?? 'Something went wrong')
          setMessages(prev =>
            prev.map(m =>
              m.id === assistantId
                ? { ...m, content: event.content ?? 'An error occurred.', isStreaming: false }
                : m
            )
          )
          break
      }
      onEvent(event)
    }
  }

  function createStreamingMessage(): [string, ChatMessageType] {
    const id = `assistant-${Date.now()}`
    return [id, {
      id,
      role: 'assistant',
      content: '',
      timestamp: new Date(),
      isStreaming: true,
    }]
  }

  async function sendMessage(text: string) {
    if (!text.trim() || isStreaming) return

    const userMessage: ChatMessageType = {
      id: `user-${Date.now()}`,
      role: 'user',
      content: text.trim(),
      timestamp: new Date(),
    }

    const [assistantId, streamingMessage] = createStreamingMessage()

    setMessages(prev => [...prev, userMessage, streamingMessage])
    setInput('')
    setIsStreaming(true)
    setActiveTool(null)

    await streamChat(
      text.trim(),
      sessionId,
      handleStreamEvents(assistantId),
      (error: string) => {
        toast.error(error)
        setMessages(prev =>
          prev.map(m =>
            m.id === assistantId
              ? { ...m, content: 'Failed to connect to AI service.', isStreaming: false }
              : m
          )
        )
      },
      () => {
        setIsStreaming(false)
        setActiveTool(null)
        setActiveAgent(null)
      }
    )
  }

  async function handleTripPlanSubmit(data: TripPlanFormData) {
    if (isStreaming) return

    const userMessage: ChatMessageType = {
      id: `user-${Date.now()}`,
      role: 'user',
      content: `Plan a trip to **${data.destination}** from ${data.startDate} to ${data.endDate}${data.budget ? ` with a budget of LKR ${data.budget.toLocaleString()}` : ''}${data.travelers ? ` for ${data.travelers} traveler(s)` : ''}${data.travelStyle ? ` — ${data.travelStyle} style` : ''}${data.interests?.length ? ` — interests: ${data.interests.join(', ')}` : ''}`,
      timestamp: new Date(),
    }

    const [assistantId, streamingMessage] = createStreamingMessage()

    setMessages(prev => [...prev, userMessage, streamingMessage])
    setIsStreaming(true)
    setActiveTool(null)

    await streamGeneratePlan(
      data,
      handleStreamEvents(assistantId),
      (error: string) => {
        toast.error(error)
        setMessages(prev =>
          prev.map(m =>
            m.id === assistantId
              ? { ...m, content: 'Failed to generate trip plan.', isStreaming: false }
              : m
          )
        )
      },
      () => {
        setIsStreaming(false)
        setActiveTool(null)
        setActiveAgent(null)
      }
    )
  }

  function handleKeyDown(e: React.KeyboardEvent) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      sendMessage(input)
    }
  }

  function handleNewChat() {
    setMessages([WELCOME_MESSAGE])
    setSessionId(null)
    setInput('')
  }

  const isWelcomeOnly = messages.length === 1 && messages[0].id === 'welcome'

  return (
    <div className="flex h-[calc(100vh-3.5rem)]">
      {/* Chat History Sidebar */}
      <ChatHistorySidebar
        open={sidebarOpen}
        onClose={() => setSidebarOpen(false)}
        currentSessionId={sessionId}
        onSelectSession={handleSelectSession}
      />

    <div className="flex flex-col flex-1 min-w-0">
      {/* Chat Header */}
      <div className="flex items-center justify-between px-4 sm:px-6 py-3 border-b border-border/50 bg-white/60 backdrop-blur-sm">
        <div className="flex items-center gap-3">
          <div className="h-9 w-9 rounded-xl bg-gradient-to-br from-primary to-teal-500 flex items-center justify-center shadow-sm">
            <Compass className="h-5 w-5 text-white" />
          </div>
          <div>
            <h2 className="text-sm font-semibold font-display">Trip Planner</h2>
            <AgentStatusBar
              isStreaming={isStreaming}
              activeAgent={activeAgent}
              activeTool={activeTool}
            />
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => setCartOpen(true)}
            className="rounded-full gap-1.5 relative"
          >
            <ShoppingCart className="h-3.5 w-3.5" />
            <span className="hidden sm:inline">Booking</span>
            {bookingItems.length > 0 && (
              <Badge className="absolute -top-1.5 -right-1.5 h-4 min-w-4 px-1 text-[10px] bg-primary">
                {bookingItems.length}
              </Badge>
            )}
          </Button>
          <Button
            variant="outline"
            size="sm"
            onClick={() => setPlanDialogOpen(true)}
            disabled={isStreaming}
            className="rounded-full gap-1.5"
          >
            <Plus className="h-3.5 w-3.5" />
            <span className="hidden sm:inline">Plan a Trip</span>
          </Button>
          <Button variant="ghost" size="sm" onClick={() => setSidebarOpen(!sidebarOpen)} className="rounded-full gap-1.5">
            <History className="h-3.5 w-3.5" />
            <span className="hidden sm:inline">History</span>
          </Button>
          <Button variant="ghost" size="sm" onClick={handleNewChat} className="rounded-full gap-1.5">
            <RotateCcw className="h-3.5 w-3.5" />
            <span className="hidden sm:inline">New</span>
          </Button>
        </div>
      </div>

      {/* Messages Area */}
      <ScrollArea className="flex-1 px-4" ref={scrollRef}>
        <div className="max-w-3xl mx-auto py-6 space-y-6">
          {/* Welcome quick-start area (shown only before first message) */}
          {isWelcomeOnly && (
            <div className="mt-4">
              <ChatMessage
                message={messages[0]}
                isLast={true}
                onQuickReply={!isStreaming ? sendMessage : undefined}
                onAddToBooking={handleAddToBooking}
              />
              <div className="mt-8 text-center">
                <p className="text-sm text-muted-foreground mb-4">Or start with a theme</p>
                <div className="flex flex-wrap gap-3 justify-center">
                  {QUICK_START_CHIPS.map(({ icon: Icon, label, value }) => (
                    <button
                      key={label}
                      onClick={() => sendMessage(value)}
                      disabled={isStreaming}
                      className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl border border-border bg-white hover:border-primary/30 hover:bg-primary/5 text-sm font-medium text-foreground transition-all duration-200 hover:shadow-sm disabled:opacity-50"
                    >
                      <Icon className="h-4 w-4 text-primary" />
                      {label}
                    </button>
                  ))}
                </div>
              </div>
            </div>
          )}

          {/* Regular messages (skip welcome when showing quick-start) */}
          {!isWelcomeOnly && messages.map((message, i) => (
            <ChatMessage
              key={message.id}
              message={message}
              isLast={i === messages.length - 1}
              onQuickReply={!isStreaming ? sendMessage : undefined}
              onAddToBooking={handleAddToBooking}
            />
          ))}
        </div>
      </ScrollArea>

      {/* Input Area */}
      <div className="border-t border-border/50 bg-white/60 backdrop-blur-sm p-3 sm:p-4">
        <div className="max-w-3xl mx-auto">
          <div className="relative flex items-end gap-2 rounded-2xl border border-border bg-white p-2 shadow-sm focus-within:border-primary/30 focus-within:shadow-md transition-all">
            <Textarea
              ref={textareaRef}
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Describe your dream trip..."
              disabled={isStreaming}
              className="min-h-[44px] max-h-32 border-0 shadow-none resize-none focus-visible:ring-0 text-sm bg-transparent"
              rows={1}
            />
            <Button
              onClick={() => sendMessage(input)}
              disabled={!input.trim() || isStreaming}
              size="sm"
              className="shrink-0 h-9 w-9 p-0 rounded-xl shadow-sm"
            >
              <Send className="h-4 w-4" />
            </Button>
          </div>
          <p className="text-xs text-muted-foreground text-center mt-2">
            AI recommendations prioritize registered Sri Lanka providers. Press Enter to send.
          </p>
        </div>
      </div>

      {/* Trip Plan Dialog */}
      <TripPlanDialog
        open={planDialogOpen}
        onOpenChange={setPlanDialogOpen}
        onSubmit={handleTripPlanSubmit}
        sessionId={sessionId}
        disabled={isStreaming}
      />

      {/* Booking Cart */}
      <BookingCart
        items={bookingItems}
        open={cartOpen}
        onOpenChange={setCartOpen}
        onRemoveItem={handleRemoveFromBooking}
        onClearAll={handleClearBooking}
      />
    </div>
    </div>
  )
}

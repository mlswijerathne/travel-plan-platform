export type EventType = 'TEXT_DELTA' | 'TOOL_CALL' | 'AGENT_TRANSFER' | 'FINAL_RESPONSE' | 'ERROR' | 'DONE'

export interface ChatStreamEvent {
  type: EventType
  content?: string
  agentName?: string
  toolName?: string
  quickReplies?: QuickReplyChip[]
  providers?: ProviderResult[]
  sessionId?: string
}

export interface QuickReplyChip {
  label: string
  value: string
}

export interface ProviderResult {
  id: string
  name: string
  type: string
  description: string
  price: number
  rating: number
  location: string
  imageUrl: string
  source: string
}

export interface ChatMessage {
  id: string
  role: 'user' | 'assistant' | 'system'
  content: string
  timestamp: Date
  quickReplies?: QuickReplyChip[]
  providers?: ProviderResult[]
  toolName?: string
  agentName?: string
  isStreaming?: boolean
}

export interface ConversationHistory {
  sessionId: string
  messages: {
    role: string
    content: string
    timestamp: string
    quickReplies?: QuickReplyChip[]
  }[]
  createdAt: string
  lastActivityAt: string
}

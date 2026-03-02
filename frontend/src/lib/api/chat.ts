import { createClient } from '@/lib/supabase/client'
import type { ChatStreamEvent } from '@/types/chat'
import type { TripPlanFormData, RecommendationRequest, RecommendationResponse } from '@/types/trip-plan'

const AI_API_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8060'

async function getToken(): Promise<string | null> {
  const supabase = createClient()
  const { data: { session } } = await supabase.auth.getSession()
  return session?.access_token ?? null
}

async function handleSSEStream(
  response: Response,
  onEvent: (event: ChatStreamEvent) => void,
  onError: (error: string) => void,
  onDone: () => void,
): Promise<void> {
  const reader = response.body?.getReader()
  if (!reader) {
    onError('No response stream')
    return
  }

  const decoder = new TextDecoder()
  let buffer = ''

  while (true) {
    const { done, value } = await reader.read()
    if (done) break

    buffer += decoder.decode(value, { stream: true })
    const lines = buffer.split('\n')
    buffer = lines.pop() || ''

    for (const line of lines) {
      if (line.startsWith('data:')) {
        const data = line.slice(5).trim()
        if (!data) continue
        try {
          const event: ChatStreamEvent = JSON.parse(data)
          onEvent(event)
          if (event.type === 'DONE') {
            onDone()
            return
          }
        } catch {
          // skip malformed events
        }
      }
    }
  }

  onDone()
}

async function postSSE(
  url: string,
  body: Record<string, unknown>,
  onEvent: (event: ChatStreamEvent) => void,
  onError: (error: string) => void,
  onDone: () => void,
): Promise<void> {
  const token = await getToken()

  try {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      'Accept': 'text/event-stream',
    }
    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }

    const response = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(body),
    })

    if (!response.ok) {
      const err = await response.text()
      onError(err || `HTTP ${response.status}`)
      return
    }

    await handleSSEStream(response, onEvent, onError, onDone)
  } catch (err) {
    onError(err instanceof Error ? err.message : 'Connection failed')
  }
}

export async function streamChat(
  message: string,
  sessionId: string | null,
  onEvent: (event: ChatStreamEvent) => void,
  onError: (error: string) => void,
  onDone: () => void,
): Promise<void> {
  await postSSE(
    `${AI_API_URL}/api/chat`,
    { message, sessionId },
    onEvent,
    onError,
    onDone,
  )
}

export async function streamGeneratePlan(
  data: TripPlanFormData,
  onEvent: (event: ChatStreamEvent) => void,
  onError: (error: string) => void,
  onDone: () => void,
): Promise<void> {
  await postSSE(
    `${AI_API_URL}/api/chat/generate-plan`,
    { ...data },
    onEvent,
    onError,
    onDone,
  )
}

export async function getRecommendations(request: RecommendationRequest): Promise<RecommendationResponse> {
  const token = await getToken()
  const response = await fetch(`${AI_API_URL}/api/chat/recommend`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
    body: JSON.stringify(request),
  })
  if (!response.ok) throw new Error(`HTTP ${response.status}`)
  const json = await response.json()
  return json.data
}

export async function getChatHistory(sessionId: string) {
  const token = await getToken()
  const response = await fetch(`${AI_API_URL}/api/chat/history?sessionId=${sessionId}`, {
    headers: { 'Authorization': `Bearer ${token}` },
  })
  if (!response.ok) throw new Error(`HTTP ${response.status}`)
  return response.json()
}

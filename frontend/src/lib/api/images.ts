import { createClient } from '@/lib/supabase/client'

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8060'

export type ImageContainer =
  | 'hotels'
  | 'vehicles'
  | 'tour-guides'
  | 'tourists'
  | 'packages'
  | 'events'
  | 'reviews'
  | 'products'

async function getAuthToken(): Promise<string | null> {
  const supabase = createClient()
  const { data: { session } } = await supabase.auth.getSession()
  return session?.access_token ?? null
}

export async function uploadImage(file: File, container: ImageContainer): Promise<string> {
  const token = await getAuthToken()
  const formData = new FormData()
  formData.append('file', file)
  formData.append('container', container)

  const res = await fetch(`${API_BASE_URL}/api/images/upload`, {
    method: 'POST',
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    body: formData,
  })

  if (!res.ok) {
    const error = await res.json().catch(() => ({ message: 'Upload failed' }))
    throw new Error(error.message || `Upload failed: HTTP ${res.status}`)
  }

  const json = await res.json()
  return json.data.url
}

export async function uploadMultipleImages(files: File[], container: ImageContainer): Promise<string[]> {
  const token = await getAuthToken()
  const formData = new FormData()
  files.forEach(file => formData.append('files', file))
  formData.append('container', container)

  const res = await fetch(`${API_BASE_URL}/api/images/upload-multiple`, {
    method: 'POST',
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    body: formData,
  })

  if (!res.ok) {
    const error = await res.json().catch(() => ({ message: 'Upload failed' }))
    throw new Error(error.message || `Upload failed: HTTP ${res.status}`)
  }

  const json = await res.json()
  return json.data.urls
}

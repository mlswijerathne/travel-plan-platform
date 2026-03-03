import { apiGet, apiPost, apiPut, buildQueryString } from './client'
import type { Order, CreateOrderRequest } from '@/types/order'

export function createOrder(body: CreateOrderRequest): Promise<Order> {
  return apiPost('/api/orders', body)
}

export function getMyOrders(params?: { page?: number; size?: number }): Promise<{
  content: Order[]
  totalElements: number
  totalPages: number
  number: number
}> {
  return apiGet(`/api/orders${buildQueryString(params ?? {})}`)
}

export function getAdminOrders(params?: { status?: string; page?: number; size?: number }): Promise<{
  content: Order[]
  totalElements: number
  totalPages: number
  number: number
}> {
  return apiGet(`/api/orders/admin${buildQueryString(params ?? {})}`)
}

export function getOrder(id: number): Promise<Order> {
  return apiGet(`/api/orders/${id}`)
}

export function updateOrderStatus(id: number, status: string): Promise<Order> {
  return apiPut(`/api/orders/${id}/status`, { status })
}

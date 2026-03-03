import { apiGet, apiPut, apiDelete, buildQueryString } from './client'
import type { ProductDTO, ProductSearchParams } from '@/types/product'

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8060'

export function getProducts(params?: ProductSearchParams): Promise<ProductDTO[]> {
  return apiGet(`/api/products${buildQueryString(params ?? {})}`)
}

export function getProduct(id: number): Promise<ProductDTO> {
  return apiGet(`/api/products/${id}`)
}

export function getProductImageUrl(filename: string): string {
  return `${API_BASE}/api/products/images/${filename}`
}

export function updateProduct(id: number, body: Partial<ProductDTO>): Promise<ProductDTO> {
  return apiPut(`/api/products/${id}`, body)
}

export function deleteProduct(id: number): Promise<void> {
  return apiDelete(`/api/products/${id}`)
}

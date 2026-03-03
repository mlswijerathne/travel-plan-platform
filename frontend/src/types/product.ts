export type ProductCategory = 'SOUVENIRS' | 'CLOTHING' | 'FOOD' | 'CRAFTS' | 'JEWELRY' | 'ART' | 'BOOKS' | 'OTHER'

export interface ProductDTO {
  id: number
  name: string
  category: string
  description?: string
  price: number
  stockQuantity: number
  imageUrl?: string
  averageRating: number
  reviewCount: number
  isActive: boolean
}

export interface CreateProductRequest {
  name: string
  category: ProductCategory
  description?: string
  price: number
  stockQuantity: number
}

export interface ProductSearchParams {
  category?: string
  minPrice?: number
  maxPrice?: number
  eventId?: string
}

export const PRODUCT_CATEGORIES: ProductCategory[] = [
  'SOUVENIRS',
  'CLOTHING',
  'FOOD',
  'CRAFTS',
  'JEWELRY',
  'ART',
  'BOOKS',
  'OTHER',
]

export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'PROCESSING' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED'
export type DeliveryType = 'PICKUP' | 'DELIVERY' | 'DIGITAL'

export interface OrderItem {
  id: number
  productId: number
  productName: string
  quantity: number
  unitPrice: number
  totalPrice: number
}

export interface Order {
  id: number
  touristId: string
  status: OrderStatus
  deliveryType: DeliveryType
  deliveryAddress?: string
  totalAmount: number
  items: OrderItem[]
  createdAt: string
  updatedAt: string
}

export interface CreateOrderRequest {
  deliveryType: DeliveryType
  deliveryAddress?: string
  items: { productId: number; quantity: number }[]
}

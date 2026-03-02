export interface ApiResponse<T> {
  data: T
  meta: {
    timestamp: string
    requestId: string | null
  }
}

export interface PaginatedResponse<T> {
  data: T[]
  pagination: {
    page: number
    pageSize: number
    totalItems: number
    totalPages: number
  }
}

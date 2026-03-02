import { type ClassValue, clsx } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function formatCurrency(amount: number, currency = 'LKR') {
  return new Intl.NumberFormat('en-LK', {
    style: 'currency',
    currency,
  }).format(amount)
}

export function formatDate(date: string | Date) {
  return new Intl.DateTimeFormat('en-LK', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  }).format(new Date(date))
}

export function formatDateShort(date: string | Date) {
  return new Intl.DateTimeFormat('en-LK', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  }).format(new Date(date))
}

export function formatDateRange(start: string | Date, end: string | Date) {
  return `${formatDateShort(start)} - ${formatDateShort(end)}`
}

export function formatRating(rating: number) {
  return rating.toFixed(1)
}

export function getBookingStatusColor(status: string) {
  switch (status) {
    case 'CONFIRMED': return 'bg-green-100 text-green-800'
    case 'PENDING': return 'bg-yellow-100 text-yellow-800'
    case 'CANCELLED': return 'bg-red-100 text-red-800'
    case 'COMPLETED': return 'bg-blue-100 text-blue-800'
    default: return 'bg-gray-100 text-gray-800'
  }
}

export function getProviderTypeLabel(type: string) {
  switch (type) {
    case 'HOTEL': return 'Hotel'
    case 'TOUR_GUIDE': return 'Tour Guide'
    case 'VEHICLE': return 'Vehicle'
    default: return type
  }
}

export function capitalize(str: string) {
  return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase()
}

export function formatAmenity(amenity: string) {
  return amenity.split('_').map(capitalize).join(' ')
}

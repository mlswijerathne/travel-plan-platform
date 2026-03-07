'use client'

import type { ProviderResult } from '@/types/chat'
import { ProviderCard } from './ProviderCard'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Badge } from '@/components/ui/badge'

interface RecommendationGroupProps {
  providers: ProviderResult[]
  onAddToBooking?: (provider: ProviderResult) => void
}

interface ProviderGroup {
  key: string
  label: string
  items: ProviderResult[]
}

function groupProviders(providers: ProviderResult[]): ProviderGroup[] {
  const groups: Record<string, ProviderResult[]> = {}
  for (const p of providers) {
    const type = p.type || 'other'
    if (!groups[type]) groups[type] = []
    groups[type].push(p)
  }

  const labelMap: Record<string, string> = {
    HOTEL: '🏨 Hotels',
    TOUR_GUIDE: '🧭 Tour Guides',
    VEHICLE: '🚗 Vehicles',
    EVENT: '🎫 Events',
    PRODUCT: '🛍️ Products',
    RESTAURANT: '🍽️ Restaurants',
    ACTIVITY: '🎯 Activities',
  }

  return Object.entries(groups).map(([key, items]) => ({
    key,
    label: labelMap[key] || key.charAt(0) + key.slice(1).toLowerCase(),
    items,
  }))
}

export function RecommendationGroup({ providers, onAddToBooking }: RecommendationGroupProps) {
  const groups = groupProviders(providers)

  if (groups.length === 0) return null

  // If only one group, show flat grid without tabs
  if (groups.length === 1) {
    return (
      <div className="w-full max-w-2xl">
        <p className="text-xs font-medium text-muted-foreground mb-2">{groups[0].label}</p>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          {groups[0].items.map((provider) => (
            <ProviderCard key={provider.id} provider={provider} onAddToBooking={onAddToBooking} />
          ))}
        </div>
      </div>
    )
  }

  return (
    <Tabs defaultValue={groups[0].key} className="w-full max-w-2xl">
      <TabsList className="h-auto flex-wrap">
        {groups.map((group) => (
          <TabsTrigger key={group.key} value={group.key} className="text-xs gap-1.5">
            {group.label}
            <Badge variant="secondary" className="h-4 min-w-4 px-1 text-[10px]">
              {group.items.length}
            </Badge>
          </TabsTrigger>
        ))}
      </TabsList>
      {groups.map((group) => (
        <TabsContent key={group.key} value={group.key}>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            {group.items.map((provider) => (
              <ProviderCard key={provider.id} provider={provider} onAddToBooking={onAddToBooking} />
            ))}
          </div>
        </TabsContent>
      ))}
    </Tabs>
  )
}

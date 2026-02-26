'use client'

import { cn } from '@/lib/utils'

interface AgentStatusBarProps {
  isStreaming: boolean
  activeAgent: string | null
  activeTool: string | null
}

const AGENT_CONFIG: Record<string, { label: string; color: string }> = {
  HotelSearchAgent: { label: 'Searching hotels', color: 'bg-blue-500' },
  TourGuideSearchAgent: { label: 'Finding guides', color: 'bg-green-500' },
  VehicleSearchAgent: { label: 'Checking transport', color: 'bg-orange-500' },
  ItineraryGeneratorAgent: { label: 'Building itinerary', color: 'bg-purple-500' },
  BudgetAnalyzerAgent: { label: 'Analyzing budget', color: 'bg-emerald-500' },
  TripPlannerAgent: { label: 'Planning trip', color: 'bg-primary' },
}

function getToolLabel(toolName: string): string {
  const labels: Record<string, string> = {
    searchHotels: 'Searching hotels',
    getHotelDetails: 'Getting hotel details',
    searchTourGuides: 'Finding tour guides',
    getGuideDetails: 'Getting guide details',
    searchVehicles: 'Searching vehicles',
    getVehicleDetails: 'Getting vehicle details',
    getDirections: 'Getting directions',
    searchNearbyPlaces: 'Searching nearby places',
    geocodeLocation: 'Looking up location',
    getDistanceMatrix: 'Calculating distances',
    getProviderReviews: 'Fetching reviews',
    searchPackages: 'Searching packages',
    getPackageDetails: 'Getting package details',
  }
  return labels[toolName] || `Running ${toolName}`
}

export function AgentStatusBar({ isStreaming, activeAgent, activeTool }: AgentStatusBarProps) {
  if (!isStreaming) {
    return <span className="text-xs text-muted-foreground">Ready to help</span>
  }

  const agentConfig = activeAgent ? AGENT_CONFIG[activeAgent] : null
  const dotColor = agentConfig?.color || 'bg-primary'

  let statusText = 'Thinking...'
  if (activeTool) {
    statusText = getToolLabel(activeTool)
  } else if (agentConfig) {
    statusText = agentConfig.label
  }

  return (
    <div className="flex items-center gap-2">
      <span className="text-xs text-primary flex items-center gap-1">
        <span className={cn('h-1.5 w-1.5 rounded-full animate-pulse', dotColor)} />
        {statusText}...
      </span>
      {activeAgent && agentConfig && (
        <span className="text-xs text-muted-foreground">
          ({activeAgent.replace('Agent', '')})
        </span>
      )}
    </div>
  )
}

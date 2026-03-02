'use client'

import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { Search, X, ShieldCheck } from 'lucide-react'
import { GUIDE_SPECIALIZATIONS, GUIDE_LANGUAGES } from '@/types/guide'
import { capitalize } from '@/lib/utils'

interface GuideFiltersProps {
  searchQuery: string
  language: string
  specialization: string
  isVerified: boolean | undefined
  onSearchChange: (q: string) => void
  onLanguageChange: (lang: string) => void
  onSpecializationChange: (spec: string) => void
  onVerifiedChange: (v: boolean | undefined) => void
  onClear: () => void
}

export function GuideFilters({
  searchQuery,
  language,
  specialization,
  isVerified,
  onSearchChange,
  onLanguageChange,
  onSpecializationChange,
  onVerifiedChange,
  onClear,
}: GuideFiltersProps) {
  const hasFilters = searchQuery || language || specialization || isVerified !== undefined

  return (
    <div className="space-y-4">
      <div className="relative">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <Input
          placeholder="Search guides by name, bio..."
          value={searchQuery}
          onChange={(e) => onSearchChange(e.target.value)}
          className="pl-10"
        />
      </div>

      <div className="space-y-3">
        <div>
          <p className="text-xs text-muted-foreground mb-1.5">Language</p>
          <div className="flex flex-wrap gap-1.5">
            {GUIDE_LANGUAGES.slice(0, 6).map((l) => (
              <Button
                key={l}
                variant={language === l ? 'default' : 'outline'}
                size="sm"
                onClick={() => onLanguageChange(language === l ? '' : l)}
                className="text-xs"
              >
                {l}
              </Button>
            ))}
          </div>
        </div>

        <div>
          <p className="text-xs text-muted-foreground mb-1.5">Specialization</p>
          <div className="flex flex-wrap gap-1.5">
            {GUIDE_SPECIALIZATIONS.map((s) => (
              <Button
                key={s}
                variant={specialization === s ? 'default' : 'outline'}
                size="sm"
                onClick={() => onSpecializationChange(specialization === s ? '' : s)}
                className="text-xs"
              >
                {s.split('_').map(capitalize).join(' ')}
              </Button>
            ))}
          </div>
        </div>
      </div>

      <div className="flex items-center gap-3">
        <Button
          variant={isVerified ? 'default' : 'outline'}
          size="sm"
          onClick={() => onVerifiedChange(isVerified ? undefined : true)}
          className="text-xs"
        >
          <ShieldCheck className="h-3 w-3 mr-1" />
          Verified Only
        </Button>
        {hasFilters && (
          <Button variant="ghost" size="sm" onClick={onClear} className="ml-auto text-xs">
            <X className="h-3 w-3 mr-1" />
            Clear filters
          </Button>
        )}
      </div>
    </div>
  )
}

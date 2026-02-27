'use client'

import { useState, useCallback } from 'react'
import { useGuides } from '@/hooks/use-guides'
import { GuideCard } from '@/components/guides/GuideCard'
import { GuideFilters } from '@/components/guides/GuideFilters'
import { Pagination } from '@/components/shared/Pagination'
import { EmptyState } from '@/components/shared/EmptyState'
import { LoadingGrid } from '@/components/shared/LoadingGrid'
import { MapPin } from 'lucide-react'

export default function GuidesPage() {
  const [page, setPage] = useState(0)
  const [searchQuery, setSearchQuery] = useState('')
  const [language, setLanguage] = useState('')
  const [specialization, setSpecialization] = useState('')
  const [isVerified, setIsVerified] = useState<boolean | undefined>(undefined)

  const { data, isLoading } = useGuides({
    query: searchQuery || undefined,
    language: language || undefined,
    specialization: specialization || undefined,
    isVerified,
    page,
    size: 9,
  })

  const guides = data?.data ?? []
  const pagination = data?.pagination

  const handleClear = useCallback(() => {
    setSearchQuery('')
    setLanguage('')
    setSpecialization('')
    setIsVerified(undefined)
    setPage(0)
  }, [])

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Tour Guides</h1>
        <p className="text-muted-foreground">Find experienced local guides for your adventure</p>
      </div>

      <GuideFilters
        searchQuery={searchQuery}
        language={language}
        specialization={specialization}
        isVerified={isVerified}
        onSearchChange={(q) => { setSearchQuery(q); setPage(0) }}
        onLanguageChange={(l) => { setLanguage(l); setPage(0) }}
        onSpecializationChange={(s) => { setSpecialization(s); setPage(0) }}
        onVerifiedChange={(v) => { setIsVerified(v); setPage(0) }}
        onClear={handleClear}
      />

      {isLoading ? (
        <LoadingGrid count={9} />
      ) : guides.length === 0 ? (
        <EmptyState
          icon={MapPin}
          title="No guides found"
          description="Try adjusting your filters to find more results."
        />
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {guides.map((guide) => (
              <GuideCard key={guide.id} guide={guide} />
            ))}
          </div>
          {pagination && (
            <Pagination page={page} totalPages={pagination.totalPages} onPageChange={setPage} />
          )}
        </>
      )}
    </div>
  )
}

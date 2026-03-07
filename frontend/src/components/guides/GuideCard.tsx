'use client'

import Link from 'next/link'
import { Star, ShieldCheck, Globe, Clock } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import type { Guide } from '@/types/guide'
import { formatCurrency, formatRating, capitalize } from '@/lib/utils'

export function GuideCard({ guide }: { guide: Guide }) {
  return (
    <Link href={`/guides/${guide.id}`} className="group block">
      <div className="rounded-xl border bg-card overflow-hidden transition-all hover:shadow-md hover:border-primary/20">
        <div className="h-36 bg-gradient-to-br from-teal-50 to-teal-100/50 flex items-center justify-center relative overflow-hidden">
          {guide.profileImageUrl ? (
            <img src={guide.profileImageUrl} alt={`${guide.firstName} ${guide.lastName}`} className="w-full h-full object-cover" />
          ) : (
            <span className="text-4xl">🧭</span>
          )}
          {guide.isVerified && (
            <div className="absolute top-2 right-2 bg-green-100 text-green-700 rounded-full p-1">
              <ShieldCheck className="h-4 w-4" />
            </div>
          )}
        </div>
        <div className="p-4">
          <h3 className="font-semibold text-foreground group-hover:text-primary transition-colors">
            {guide.firstName} {guide.lastName}
          </h3>

          <div className="flex items-center gap-3 mt-1">
            {guide.reviewCount > 0 && (
              <div className="flex items-center gap-1 text-sm">
                <Star className="h-3.5 w-3.5 fill-amber-400 text-amber-400" />
                <span className="font-medium">{formatRating(guide.averageRating)}</span>
                <span className="text-muted-foreground">({guide.reviewCount})</span>
              </div>
            )}
            {guide.experienceYears != null && guide.experienceYears > 0 && (
              <div className="flex items-center gap-1 text-xs text-muted-foreground">
                <Clock className="h-3 w-3" />
                {guide.experienceYears}yr exp
              </div>
            )}
          </div>

          {guide.languages.length > 0 && (
            <div className="flex items-center gap-1 mt-2 text-xs text-muted-foreground">
              <Globe className="h-3 w-3 shrink-0" />
              <span className="truncate">{guide.languages.join(', ')}</span>
            </div>
          )}

          {guide.specializations.length > 0 && (
            <div className="flex flex-wrap gap-1 mt-2">
              {guide.specializations.slice(0, 3).map((s) => (
                <Badge key={s} variant="secondary" className="text-[10px] px-1.5 py-0">
                  {s.split('_').map(capitalize).join(' ')}
                </Badge>
              ))}
            </div>
          )}

          <div className="mt-3 pt-3 border-t flex items-center justify-between">
            <span className="text-sm font-medium text-primary">
              {formatCurrency(guide.dailyRate)}<span className="text-xs font-normal text-muted-foreground">/day</span>
            </span>
            <span className="text-xs text-muted-foreground">
              {formatCurrency(guide.hourlyRate)}/hr
            </span>
          </div>
        </div>
      </div>
    </Link>
  )
}

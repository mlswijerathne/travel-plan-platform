'use client'

import { use, useState } from 'react'
import { useGuide, useGuideAvailability } from '@/hooks/use-guides'
import { ReviewSection } from '@/components/shared/ReviewSection'
import { StarRating } from '@/components/shared/StarRating'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { ArrowLeft, Star, Globe, Clock, ShieldCheck, Mail, Phone, CheckCircle, XCircle, MapPin } from 'lucide-react'
import { formatCurrency, formatRating, capitalize } from '@/lib/utils'
import Link from 'next/link'

export default function GuideDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params)
  const guideId = Number(id)
  const { data, isLoading } = useGuide(guideId)
  const guide = data?.data

  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const { data: availData, isLoading: checkingAvail } = useGuideAvailability(guideId, startDate, endDate)
  const availability = availData?.data

  if (isLoading) {
    return (
      <div className="animate-pulse space-y-6">
        <div className="h-8 bg-muted rounded w-48" />
        <div className="h-64 bg-muted rounded-xl" />
      </div>
    )
  }

  if (!guide) {
    return (
      <div className="text-center py-16">
        <p className="text-muted-foreground">Guide not found</p>
        <Link href="/guides">
          <Button variant="outline" className="mt-4">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Guides
          </Button>
        </Link>
      </div>
    )
  }

  return (
    <div className="space-y-8">
      <Link href="/guides" className="inline-flex items-center text-sm text-muted-foreground hover:text-foreground">
        <ArrowLeft className="h-4 w-4 mr-1" />
        Back to Guides
      </Link>

      {/* Profile Header */}
      <div className="rounded-xl border border-border/30 bg-card shadow-editorial overflow-hidden">
        <div className="h-40 md:h-48 bg-gradient-to-br from-primary/10 to-primary/20" />
        <div className="p-6 -mt-12">
          <div className="flex flex-col sm:flex-row gap-4 items-start">
            <div className="h-20 w-20 rounded-full bg-card ring-4 ring-card shadow-md flex items-center justify-center overflow-hidden">
              {guide.profileImageUrl ? (
                <img src={guide.profileImageUrl} alt={`${guide.firstName} ${guide.lastName}`} className="w-full h-full object-cover" />
              ) : (
                <MapPin className="h-12 w-12 text-primary/30" />
              )}
            </div>
            <div className="flex-1">
              <div className="flex items-center gap-2">
                <h1 className="font-display text-2xl font-extrabold tracking-tight text-foreground">{guide.firstName} {guide.lastName}</h1>
                {guide.isVerified && (
                  <Badge className="bg-tertiary/10 text-tertiary border-tertiary/30">
                    <ShieldCheck className="h-3 w-3 mr-1" />
                    Verified
                  </Badge>
                )}
              </div>
              {guide.reviewCount > 0 && (
                <div className="flex items-center gap-2 mt-1">
                  <Star className="h-4 w-4 fill-secondary text-secondary" />
                  <span className="font-medium">{formatRating(guide.averageRating)}</span>
                  <span className="text-sm text-muted-foreground">({guide.reviewCount} reviews)</span>
                </div>
              )}
            </div>
          </div>

          {guide.bio && <p className="text-muted-foreground mt-4">{guide.bio}</p>}

          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mt-6">
            {guide.experienceYears != null && (
              <div className="text-center p-3 bg-surface-low rounded-xl">
                <Clock className="h-5 w-5 mx-auto text-muted-foreground mb-1" />
                <p className="text-lg font-bold">{guide.experienceYears}</p>
                <p className="text-xs text-muted-foreground">Years Exp.</p>
              </div>
            )}
            <div className="text-center p-3 bg-surface-low rounded-xl">
              <p className="text-lg font-bold text-primary">{formatCurrency(guide.hourlyRate)}</p>
              <p className="text-xs text-muted-foreground">Per Hour</p>
            </div>
            <div className="text-center p-3 bg-surface-low rounded-xl">
              <p className="text-lg font-bold text-primary">{formatCurrency(guide.dailyRate)}</p>
              <p className="text-xs text-muted-foreground">Per Day</p>
            </div>
            <div className="text-center p-3 bg-surface-low rounded-xl">
              <p className="text-lg font-bold">{guide.reviewCount}</p>
              <p className="text-xs text-muted-foreground">Reviews</p>
            </div>
          </div>
        </div>
      </div>

      {/* Details */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="rounded-xl border bg-card p-6 space-y-4">
          <h2 className="font-semibold">Contact & Details</h2>
          {guide.email && (
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <Mail className="h-4 w-4" />
              {guide.email}
            </div>
          )}
          {guide.phoneNumber && (
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <Phone className="h-4 w-4" />
              {guide.phoneNumber}
            </div>
          )}
          {guide.languages.length > 0 && (
            <div>
              <p className="text-xs text-muted-foreground mb-1.5">Languages</p>
              <div className="flex items-center gap-1">
                <Globe className="h-4 w-4 text-muted-foreground" />
                <span className="text-sm">{guide.languages.join(', ')}</span>
              </div>
            </div>
          )}
        </div>

        <div className="rounded-xl border bg-card p-6 space-y-4">
          <h2 className="font-semibold">Specializations</h2>
          {guide.specializations.length > 0 ? (
            <div className="flex flex-wrap gap-1.5">
              {guide.specializations.map((s) => (
                <Badge key={s} variant="secondary">{s.split('_').map(capitalize).join(' ')}</Badge>
              ))}
            </div>
          ) : (
            <p className="text-sm text-muted-foreground">No specializations listed</p>
          )}
        </div>
      </div>

      {/* Availability Check */}
      <div className="rounded-xl bg-card shadow-editorial border border-border/30 p-6">
        <h2 className="font-display text-xl font-bold text-foreground mb-4">Check Availability</h2>
        <div className="flex flex-col sm:flex-row gap-3">
          <div className="flex-1">
            <label className="text-sm text-muted-foreground mb-1 block">Start Date</label>
            <Input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} />
          </div>
          <div className="flex-1">
            <label className="text-sm text-muted-foreground mb-1 block">End Date</label>
            <Input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} />
          </div>
        </div>
        {checkingAvail && <p className="text-sm text-muted-foreground mt-3">Checking availability...</p>}
        {availability && (
          <div className="mt-3 flex items-center gap-2">
            {availability.available ? (
              <>
                <CheckCircle className="h-5 w-5 text-tertiary" />
                <span className="text-sm text-tertiary">
                  Available! Daily rate: {formatCurrency(availability.dailyRate)}
                </span>
              </>
            ) : (
              <>
                <XCircle className="h-5 w-5 text-destructive" />
                <span className="text-sm text-destructive">Not available for selected dates</span>
              </>
            )}
          </div>
        )}
      </div>

      {/* Reviews */}
      <ReviewSection entityType="TOUR_GUIDE" entityId={guideId} />
    </div>
  )
}

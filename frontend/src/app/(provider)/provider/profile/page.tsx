'use client'

import { useMyGuideProfile, useUpdateMyGuideProfile, useRegisterGuide } from '@/hooks/use-guides'
import { GuideProfileForm } from '@/components/provider/GuideProfileForm'
import { ArrowLeft } from 'lucide-react'
import Link from 'next/link'

export default function GuideProfilePage() {
  const { data, isLoading } = useMyGuideProfile()
  const updateMutation = useUpdateMyGuideProfile()
  const registerMutation = useRegisterGuide()
  const guide = data?.data

  if (isLoading) {
    return (
      <div className="animate-pulse space-y-6">
        <div className="h-8 bg-muted rounded w-48" />
        <div className="h-96 bg-muted rounded-xl" />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <Link href="/provider/dashboard" className="inline-flex items-center text-sm text-muted-foreground hover:text-foreground">
        <ArrowLeft className="h-4 w-4 mr-1" />
        Back to Dashboard
      </Link>

      <div>
        <h1 className="text-2xl font-bold">{guide ? 'Edit Guide Profile' : 'Register as Tour Guide'}</h1>
        <p className="text-muted-foreground">
          {guide ? 'Update your profile information' : 'Set up your guide profile to start receiving bookings'}
        </p>
      </div>

      <div className="rounded-xl border bg-card p-6">
        <GuideProfileForm
          guide={guide ?? undefined}
          isPending={updateMutation.isPending || registerMutation.isPending}
          onSubmit={(data) => {
            if (guide) {
              updateMutation.mutate(data)
            } else {
              registerMutation.mutate(data)
            }
          }}
        />
      </div>
    </div>
  )
}

'use client'

import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { GUIDE_SPECIALIZATIONS, GUIDE_LANGUAGES } from '@/types/guide'
import { capitalize } from '@/lib/utils'
import { uploadImage } from '@/lib/api/images'
import type { Guide } from '@/types/guide'

const guideSchema = z.object({
  firstName: z.string().min(1, 'First name is required').max(100),
  lastName: z.string().min(1, 'Last name is required').max(100),
  email: z.string().email('Valid email required'),
  phoneNumber: z.string().max(20).optional(),
  bio: z.string().optional(),
  experienceYears: z.coerce.number().min(0).optional(),
  hourlyRate: z.coerce.number().min(0.01, 'Hourly rate must be positive'),
  dailyRate: z.coerce.number().min(0.01, 'Daily rate must be positive'),
  languages: z.array(z.string()).optional(),
  specializations: z.array(z.string()).optional(),
})

type GuideFormData = z.infer<typeof guideSchema>

interface GuideProfileFormProps {
  guide?: Guide
  onSubmit: (data: GuideFormData & { profileImageUrl?: string }) => void
  isPending: boolean
}

export function GuideProfileForm({ guide, onSubmit, isPending }: GuideProfileFormProps) {
  const [imageFile, setImageFile] = useState<File | null>(null)
  const [uploading, setUploading] = useState(false)
  const { register, handleSubmit, watch, setValue, formState: { errors } } = useForm<GuideFormData>({
    resolver: zodResolver(guideSchema),
    defaultValues: {
      firstName: guide?.firstName ?? '',
      lastName: guide?.lastName ?? '',
      email: guide?.email ?? '',
      phoneNumber: guide?.phoneNumber ?? '',
      bio: guide?.bio ?? '',
      experienceYears: guide?.experienceYears ?? 0,
      hourlyRate: guide?.hourlyRate ?? 0,
      dailyRate: guide?.dailyRate ?? 0,
      languages: guide?.languages ?? [],
      specializations: guide?.specializations ?? [],
    },
  })

  const selectedLanguages = watch('languages') ?? []
  const selectedSpecs = watch('specializations') ?? []

  async function handleFormSubmit(data: GuideFormData) {
    let profileImageUrl = guide?.profileImageUrl ?? undefined
    if (imageFile) {
      setUploading(true)
      try {
        profileImageUrl = await uploadImage(imageFile, 'tour-guides')
      } finally {
        setUploading(false)
      }
    }
    onSubmit({ ...data, profileImageUrl })
  }

  function toggleItem(field: 'languages' | 'specializations', item: string) {
    const current = field === 'languages' ? selectedLanguages : selectedSpecs
    const updated = current.includes(item)
      ? current.filter((i) => i !== item)
      : [...current, item]
    setValue(field, updated)
  }

  return (
    <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-6">
      <div>
        <label className="text-sm font-medium mb-1 block">Profile Photo</label>
        {guide?.profileImageUrl && !imageFile && (
          <img src={guide.profileImageUrl} alt="Current profile" className="h-24 w-24 object-cover rounded-full mb-2" />
        )}
        <Input
          type="file"
          accept="image/*"
          onChange={(e) => e.target.files?.[0] && setImageFile(e.target.files[0])}
        />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label className="text-sm font-medium mb-1 block">First Name *</label>
          <Input {...register('firstName')} />
          {errors.firstName && <p className="text-xs text-destructive mt-1">{errors.firstName.message}</p>}
        </div>
        <div>
          <label className="text-sm font-medium mb-1 block">Last Name *</label>
          <Input {...register('lastName')} />
          {errors.lastName && <p className="text-xs text-destructive mt-1">{errors.lastName.message}</p>}
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label className="text-sm font-medium mb-1 block">Email *</label>
          <Input type="email" {...register('email')} />
          {errors.email && <p className="text-xs text-destructive mt-1">{errors.email.message}</p>}
        </div>
        <div>
          <label className="text-sm font-medium mb-1 block">Phone</label>
          <Input {...register('phoneNumber')} placeholder="+94..." />
        </div>
      </div>

      <div>
        <label className="text-sm font-medium mb-1 block">Bio</label>
        <Textarea {...register('bio')} placeholder="Tell tourists about yourself..." rows={4} />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div>
          <label className="text-sm font-medium mb-1 block">Experience (years)</label>
          <Input type="number" {...register('experienceYears')} min={0} />
        </div>
        <div>
          <label className="text-sm font-medium mb-1 block">Hourly Rate (LKR) *</label>
          <Input type="number" step="0.01" {...register('hourlyRate')} />
          {errors.hourlyRate && <p className="text-xs text-destructive mt-1">{errors.hourlyRate.message}</p>}
        </div>
        <div>
          <label className="text-sm font-medium mb-1 block">Daily Rate (LKR) *</label>
          <Input type="number" step="0.01" {...register('dailyRate')} />
          {errors.dailyRate && <p className="text-xs text-destructive mt-1">{errors.dailyRate.message}</p>}
        </div>
      </div>

      <div>
        <label className="text-sm font-medium mb-2 block">Languages</label>
        <div className="flex flex-wrap gap-2">
          {GUIDE_LANGUAGES.map((l) => (
            <Button
              key={l}
              type="button"
              variant={selectedLanguages.includes(l) ? 'default' : 'outline'}
              size="sm"
              onClick={() => toggleItem('languages', l)}
            >
              {l}
            </Button>
          ))}
        </div>
      </div>

      <div>
        <label className="text-sm font-medium mb-2 block">Specializations</label>
        <div className="flex flex-wrap gap-2">
          {GUIDE_SPECIALIZATIONS.map((s) => (
            <Button
              key={s}
              type="button"
              variant={selectedSpecs.includes(s) ? 'default' : 'outline'}
              size="sm"
              onClick={() => toggleItem('specializations', s)}
            >
              {s.split('_').map(capitalize).join(' ')}
            </Button>
          ))}
        </div>
      </div>

      <div className="flex justify-end">
        <Button type="submit" disabled={isPending || uploading}>
          {uploading ? 'Uploading photo...' : isPending ? 'Saving...' : guide ? 'Update Profile' : 'Register as Guide'}
        </Button>
      </div>
    </form>
  )
}

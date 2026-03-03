'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { createPackage } from '@/lib/api/packages'
import type { PackageRequest } from '@/lib/api/packages'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Switch } from '@/components/ui/switch'
import { Badge } from '@/components/ui/badge'
import { ArrowLeft, Plus, X } from 'lucide-react'
import { toast } from 'sonner'
import Link from 'next/link'

function TagInput({
  label,
  values,
  onChange,
  placeholder,
}: {
  label: string
  values: string[]
  onChange: (v: string[]) => void
  placeholder: string
}) {
  const [input, setInput] = useState('')

  function add() {
    const trimmed = input.trim()
    if (trimmed && !values.includes(trimmed)) {
      onChange([...values, trimmed])
    }
    setInput('')
  }

  return (
    <div className="space-y-2">
      <Label>{label}</Label>
      <div className="flex gap-2">
        <Input
          value={input}
          onChange={e => setInput(e.target.value)}
          onKeyDown={e => { if (e.key === 'Enter') { e.preventDefault(); add() } }}
          placeholder={placeholder}
          className="h-10"
        />
        <Button type="button" variant="outline" size="sm" onClick={add} className="h-10 px-3">
          <Plus className="h-4 w-4" />
        </Button>
      </div>
      {values.length > 0 && (
        <div className="flex flex-wrap gap-1.5">
          {values.map(v => (
            <Badge key={v} variant="secondary" className="gap-1 pr-1">
              {v}
              <button type="button" onClick={() => onChange(values.filter(x => x !== v))} className="hover:text-destructive">
                <X className="h-3 w-3" />
              </button>
            </Badge>
          ))}
        </div>
      )}
    </div>
  )
}

export default function NewPackagePage() {
  const router = useRouter()
  const [saving, setSaving] = useState(false)

  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [durationDays, setDurationDays] = useState(1)
  const [basePrice, setBasePrice] = useState(0)
  const [discountPercentage, setDiscountPercentage] = useState(0)
  const [maxParticipants, setMaxParticipants] = useState(10)
  const [isFeatured, setIsFeatured] = useState(false)
  const [destinations, setDestinations] = useState<string[]>([])
  const [inclusions, setInclusions] = useState<string[]>([])
  const [exclusions, setExclusions] = useState<string[]>([])
  const [images, setImages] = useState<string[]>([])

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!name || !description || destinations.length === 0) {
      toast.error('Name, description, and at least one destination are required')
      return
    }
    setSaving(true)
    try {
      const body: PackageRequest = {
        name, description, durationDays, basePrice, discountPercentage,
        maxParticipants, isFeatured, destinations, inclusions, exclusions, images,
      }
      await createPackage(body)
      toast.success('Package created successfully')
      router.push('/admin/packages')
    } catch {
      toast.error('Failed to create package')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="space-y-6 max-w-3xl">
      <div className="flex items-center gap-3">
        <Link href="/admin/packages">
          <Button variant="ghost" size="sm" className="gap-2">
            <ArrowLeft className="h-4 w-4" />
            Back
          </Button>
        </Link>
        <div>
          <h1 className="font-display text-2xl font-bold">New Package</h1>
          <p className="text-muted-foreground text-sm">Create a new curated travel package</p>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        <Card className="shadow-sm">
          <CardHeader>
            <CardTitle className="text-base">Basic Information</CardTitle>
          </CardHeader>
          <CardContent className="space-y-5">
            <div className="space-y-2">
              <Label>Package Name *</Label>
              <Input value={name} onChange={e => setName(e.target.value)} placeholder="e.g. Cultural Triangle Explorer" className="h-11" required />
            </div>
            <div className="space-y-2">
              <Label>Description *</Label>
              <Textarea value={description} onChange={e => setDescription(e.target.value)} placeholder="Describe this package..." rows={4} required />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>Duration (days) *</Label>
                <Input type="number" min={1} value={durationDays} onChange={e => setDurationDays(+e.target.value)} className="h-11" required />
              </div>
              <div className="space-y-2">
                <Label>Max Participants *</Label>
                <Input type="number" min={1} value={maxParticipants} onChange={e => setMaxParticipants(+e.target.value)} className="h-11" required />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>Base Price (Rs.) *</Label>
                <Input type="number" min={0} value={basePrice} onChange={e => setBasePrice(+e.target.value)} className="h-11" required />
              </div>
              <div className="space-y-2">
                <Label>Discount (%)</Label>
                <Input type="number" min={0} max={100} value={discountPercentage} onChange={e => setDiscountPercentage(+e.target.value)} className="h-11" />
              </div>
            </div>
            <div className="flex items-center gap-3">
              <Switch checked={isFeatured} onCheckedChange={setIsFeatured} id="featured" />
              <Label htmlFor="featured">Mark as Featured</Label>
            </div>
          </CardContent>
        </Card>

        <Card className="shadow-sm">
          <CardHeader>
            <CardTitle className="text-base">Destinations & Details</CardTitle>
            <CardDescription>Press Enter or click + to add items</CardDescription>
          </CardHeader>
          <CardContent className="space-y-5">
            <TagInput label="Destinations *" values={destinations} onChange={setDestinations} placeholder="e.g. Sigiriya" />
            <TagInput label="Inclusions" values={inclusions} onChange={setInclusions} placeholder="e.g. Accommodation" />
            <TagInput label="Exclusions" values={exclusions} onChange={setExclusions} placeholder="e.g. Flights" />
            <TagInput label="Image URLs" values={images} onChange={setImages} placeholder="https://..." />
          </CardContent>
        </Card>

        <div className="flex justify-end gap-3">
          <Link href="/admin/packages">
            <Button type="button" variant="outline">Cancel</Button>
          </Link>
          <Button type="submit" disabled={saving}>
            {saving ? 'Creating...' : 'Create Package'}
          </Button>
        </div>
      </form>
    </div>
  )
}

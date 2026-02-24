'use client'

import { useEffect, useState } from 'react'
import { getCurrentTourist, updateTouristProfile, updateTouristPreferences } from '@/lib/api/tourist'
import type { Tourist } from '@/types/tourist'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import { Avatar, AvatarFallback } from '@/components/ui/avatar'
import { toast } from 'sonner'
import {
  User,
  Mail,
  Phone,
  Globe,
  Save,
  Sparkles,
  Heart,
  Languages,
  Accessibility,
  Calendar,
} from 'lucide-react'

const INTERESTS = ['Adventure', 'Culture', 'Nature', 'Food', 'Relaxation', 'Nightlife']
const LANGUAGES = ['English', 'Spanish', 'French', 'German', 'Chinese', 'Japanese', 'Sinhala', 'Tamil']
const BUDGET_LEVELS = ['BUDGET', 'MODERATE', 'LUXURY']
const TRAVEL_STYLES = ['Solo', 'Couple', 'Family', 'Group', 'Backpacker']

const INTEREST_COLORS: Record<string, string> = {
  Adventure: 'bg-orange-50 text-orange-600 border-orange-200 hover:bg-orange-100',
  Culture: 'bg-violet-50 text-violet-600 border-violet-200 hover:bg-violet-100',
  Nature: 'bg-emerald-50 text-emerald-600 border-emerald-200 hover:bg-emerald-100',
  Food: 'bg-amber-50 text-amber-600 border-amber-200 hover:bg-amber-100',
  Relaxation: 'bg-blue-50 text-blue-600 border-blue-200 hover:bg-blue-100',
  Nightlife: 'bg-pink-50 text-pink-600 border-pink-200 hover:bg-pink-100',
}

export default function ProfilePage() {
  const [tourist, setTourist] = useState<Tourist | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)

  const [firstName, setFirstName] = useState('')
  const [lastName, setLastName] = useState('')
  const [phoneNumber, setPhoneNumber] = useState('')
  const [nationality, setNationality] = useState('')

  const [preferredBudget, setPreferredBudget] = useState('')
  const [travelStyle, setTravelStyle] = useState('')
  const [interests, setInterests] = useState<string[]>([])
  const [preferredLanguages, setPreferredLanguages] = useState<string[]>([])
  const [accessibilityNeeds, setAccessibilityNeeds] = useState('')

  useEffect(() => { loadProfile() }, [])

  async function loadProfile() {
    try {
      const res = await getCurrentTourist()
      const t = res.data
      setTourist(t)
      setFirstName(t.firstName)
      setLastName(t.lastName)
      setPhoneNumber(t.phoneNumber ?? '')
      setNationality(t.nationality ?? '')
      if (t.preferences) {
        setPreferredBudget(t.preferences.preferredBudget ?? '')
        setTravelStyle(t.preferences.travelStyle ?? '')
        setInterests(t.preferences.interests ?? [])
        setPreferredLanguages(t.preferences.preferredLanguages ?? [])
        setAccessibilityNeeds(t.preferences.accessibilityNeeds ?? '')
      }
    } catch {
      toast.error('Failed to load profile')
    } finally {
      setLoading(false)
    }
  }

  async function handleProfileSave(e: React.FormEvent) {
    e.preventDefault()
    setSaving(true)
    try {
      const res = await updateTouristProfile({ firstName, lastName, phoneNumber, nationality })
      setTourist(res.data)
      toast.success('Profile updated')
    } catch {
      toast.error('Failed to update profile')
    } finally {
      setSaving(false)
    }
  }

  async function handlePreferencesSave(e: React.FormEvent) {
    e.preventDefault()
    setSaving(true)
    try {
      await updateTouristPreferences({
        preferredBudget, travelStyle, interests, preferredLanguages,
        accessibilityNeeds: accessibilityNeeds || undefined,
      })
      toast.success('Preferences updated')
    } catch {
      toast.error('Failed to update preferences')
    } finally {
      setSaving(false)
    }
  }

  function toggleChip(list: string[], item: string, setter: (v: string[]) => void) {
    setter(list.includes(item) ? list.filter(i => i !== item) : [...list, item])
  }

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="flex items-center gap-4">
          <Skeleton className="h-16 w-16 rounded-full" />
          <div className="space-y-2">
            <Skeleton className="h-6 w-48" />
            <Skeleton className="h-4 w-32" />
          </div>
        </div>
        <Skeleton className="h-64 w-full rounded-xl" />
      </div>
    )
  }

  if (!tourist) {
    return (
      <Card className="border-dashed">
        <CardContent className="py-16 text-center">
          <User className="h-12 w-12 text-muted-foreground/40 mx-auto mb-4" />
          <p className="text-muted-foreground font-medium">Profile not found. Please register first.</p>
        </CardContent>
      </Card>
    )
  }

  const initials = `${tourist.firstName?.[0] ?? ''}${tourist.lastName?.[0] ?? ''}`.toUpperCase()

  return (
    <div className="space-y-6">
      {/* Profile Header */}
      <Card className="border-0 bg-gradient-to-r from-primary/5 via-teal-50/50 to-emerald-50/30 shadow-sm">
        <CardContent className="flex items-center gap-5 py-6">
          <Avatar className="h-16 w-16 ring-4 ring-white shadow-md">
            <AvatarFallback className="bg-gradient-to-br from-primary to-teal-500 text-white text-xl font-bold">
              {initials}
            </AvatarFallback>
          </Avatar>
          <div>
            <h1 className="font-display text-2xl font-bold text-foreground">
              {tourist.firstName} {tourist.lastName}
            </h1>
            <div className="flex items-center gap-4 mt-1 text-sm text-muted-foreground">
              <span className="flex items-center gap-1">
                <Mail className="h-3.5 w-3.5" />
                {tourist.email}
              </span>
              {tourist.createdAt && (
                <span className="flex items-center gap-1">
                  <Calendar className="h-3.5 w-3.5" />
                  Member since {new Date(tourist.createdAt).getFullYear()}
                </span>
              )}
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Tabs */}
      <Tabs defaultValue="profile" className="space-y-6">
        <TabsList className="bg-white border shadow-sm">
          <TabsTrigger value="profile" className="gap-2">
            <User className="h-4 w-4" />
            Profile
          </TabsTrigger>
          <TabsTrigger value="preferences" className="gap-2">
            <Sparkles className="h-4 w-4" />
            Preferences
          </TabsTrigger>
        </TabsList>

        <TabsContent value="profile">
          <Card className="shadow-sm">
            <CardHeader>
              <CardTitle className="font-display text-lg">Personal Information</CardTitle>
              <CardDescription>Update your profile details to personalize your experience</CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleProfileSave} className="space-y-5 max-w-lg">
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label className="flex items-center gap-1.5">
                      <User className="h-3.5 w-3.5 text-muted-foreground" />
                      First Name
                    </Label>
                    <Input value={firstName} onChange={(e) => setFirstName(e.target.value)} className="h-11" />
                  </div>
                  <div className="space-y-2">
                    <Label className="flex items-center gap-1.5">
                      <User className="h-3.5 w-3.5 text-muted-foreground" />
                      Last Name
                    </Label>
                    <Input value={lastName} onChange={(e) => setLastName(e.target.value)} className="h-11" />
                  </div>
                </div>
                <div className="space-y-2">
                  <Label className="flex items-center gap-1.5">
                    <Phone className="h-3.5 w-3.5 text-muted-foreground" />
                    Phone Number
                  </Label>
                  <Input value={phoneNumber} onChange={(e) => setPhoneNumber(e.target.value)} placeholder="+94 77 123 4567" className="h-11" />
                </div>
                <div className="space-y-2">
                  <Label className="flex items-center gap-1.5">
                    <Globe className="h-3.5 w-3.5 text-muted-foreground" />
                    Nationality
                  </Label>
                  <Input value={nationality} onChange={(e) => setNationality(e.target.value)} className="h-11" />
                </div>
                <div className="pt-2">
                  <Button type="submit" disabled={saving} className="gap-2">
                    <Save className="h-4 w-4" />
                    {saving ? 'Saving...' : 'Save Changes'}
                  </Button>
                </div>
              </form>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="preferences">
          <Card className="shadow-sm">
            <CardHeader>
              <CardTitle className="font-display text-lg">Travel Preferences</CardTitle>
              <CardDescription>Help our AI tailor recommendations to your taste</CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handlePreferencesSave} className="space-y-6 max-w-lg">
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label>Budget Level</Label>
                    <Select value={preferredBudget} onValueChange={setPreferredBudget}>
                      <SelectTrigger className="h-11"><SelectValue placeholder="Select..." /></SelectTrigger>
                      <SelectContent>
                        {BUDGET_LEVELS.map(b => (
                          <SelectItem key={b} value={b}>{b.charAt(0) + b.slice(1).toLowerCase()}</SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                  <div className="space-y-2">
                    <Label>Travel Style</Label>
                    <Select value={travelStyle} onValueChange={setTravelStyle}>
                      <SelectTrigger className="h-11"><SelectValue placeholder="Select..." /></SelectTrigger>
                      <SelectContent>
                        {TRAVEL_STYLES.map(s => (
                          <SelectItem key={s} value={s}>{s}</SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                </div>

                <div className="space-y-3">
                  <Label className="flex items-center gap-1.5">
                    <Heart className="h-3.5 w-3.5 text-muted-foreground" />
                    Interests
                  </Label>
                  <div className="flex flex-wrap gap-2">
                    {INTERESTS.map(interest => (
                      <Badge
                        key={interest}
                        variant="outline"
                        className={`cursor-pointer transition-all duration-200 px-3 py-1.5 text-sm ${
                          interests.includes(interest)
                            ? INTEREST_COLORS[interest] ?? 'bg-primary/10 text-primary border-primary/30'
                            : 'bg-white text-muted-foreground border-border hover:bg-muted'
                        }`}
                        onClick={() => toggleChip(interests, interest, setInterests)}
                      >
                        {interest}
                      </Badge>
                    ))}
                  </div>
                </div>

                <div className="space-y-3">
                  <Label className="flex items-center gap-1.5">
                    <Languages className="h-3.5 w-3.5 text-muted-foreground" />
                    Preferred Languages
                  </Label>
                  <div className="flex flex-wrap gap-2">
                    {LANGUAGES.map(lang => (
                      <Badge
                        key={lang}
                        variant="outline"
                        className={`cursor-pointer transition-all duration-200 px-3 py-1.5 text-sm ${
                          preferredLanguages.includes(lang)
                            ? 'bg-primary/10 text-primary border-primary/30'
                            : 'bg-white text-muted-foreground border-border hover:bg-muted'
                        }`}
                        onClick={() => toggleChip(preferredLanguages, lang, setPreferredLanguages)}
                      >
                        {lang}
                      </Badge>
                    ))}
                  </div>
                </div>

                <div className="space-y-2">
                  <Label className="flex items-center gap-1.5">
                    <Accessibility className="h-3.5 w-3.5 text-muted-foreground" />
                    Accessibility Needs
                  </Label>
                  <Input
                    value={accessibilityNeeds}
                    onChange={(e) => setAccessibilityNeeds(e.target.value)}
                    placeholder="Any special requirements..."
                    className="h-11"
                  />
                </div>

                <div className="pt-2">
                  <Button type="submit" disabled={saving} className="gap-2">
                    <Save className="h-4 w-4" />
                    {saving ? 'Saving...' : 'Save Preferences'}
                  </Button>
                </div>
              </form>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}

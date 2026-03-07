'use client'

import { useState } from 'react'
import { useParams, useRouter } from 'next/navigation'
import {
  useItinerary,
  useItineraryDays,
  useActivateItinerary,
  useCompleteItinerary,
  useDeleteItinerary,
  useGenerateDays,
  useAddActivity,
  useDeleteActivity,
  useExpenses,
  useExpenseSummary,
  useAddExpense,
  useDeleteExpense,
} from '@/hooks/use-itineraries'
import type { CreateExpenseRequest, ItineraryDay } from '@/types/itinerary'
import { EXPENSE_CATEGORIES } from '@/types/itinerary'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from '@/components/ui/alert-dialog'
import {
  ArrowLeft,
  Calendar,
  Clock,
  Download,
  Play,
  CheckCircle2,
  Trash2,
  Plus,
  DollarSign,
  PieChart,
  Map,
  CalendarDays,
  MapPin,
  Hotel,
  Car,
  Users,
  Activity,
} from 'lucide-react'
import { toast } from 'sonner'
import Link from 'next/link'
import { createClient } from '@/lib/supabase/client'
import { getItineraryPdfUrl } from '@/lib/api/itinerary'

const STATUS_COLORS: Record<string, string> = {
  PLANNING: 'bg-blue-50 text-blue-600 border-blue-200',
  ACTIVE: 'bg-emerald-50 text-emerald-600 border-emerald-200',
  COMPLETED: 'bg-gray-50 text-gray-500 border-gray-200',
  CANCELLED: 'bg-red-50 text-red-500 border-red-200',
}

const ACTIVITY_ICONS: Record<string, any> = {
  ACCOMMODATION: Hotel,
  TRANSPORT: Car,
  GUIDE: Users,
  ACTIVITY: Activity,
  CUSTOM: MapPin,
}

export default function ItineraryDetailPage() {
  const { id } = useParams<{ id: string }>()
  const itineraryId = Number(id)
  const router = useRouter()

  const { data: itin, isLoading } = useItinerary(itineraryId)
  const { data: days, isLoading: daysLoading } = useItineraryDays(itineraryId)
  const { data: expenses = [] } = useExpenses(itineraryId)
  const { data: summary } = useExpenseSummary(itineraryId)

  const activateMutation = useActivateItinerary()
  const completeMutation = useCompleteItinerary()
  const deleteMutation = useDeleteItinerary()
  const generateDaysMutation = useGenerateDays()
  const addActivityMutation = useAddActivity()
  const deleteActivityMutation = useDeleteActivity()
  const addExpenseMutation = useAddExpense()
  const deleteExpenseMutation = useDeleteExpense()

  const [addExpenseOpen, setAddExpenseOpen] = useState(false)
  const [addActivityOpen, setAddActivityOpen] = useState(false)
  const [selectedDayId, setSelectedDayId] = useState<number | null>(null)

  // Expense form
  const [expCategory, setExpCategory] = useState('')
  const [expDescription, setExpDescription] = useState('')
  const [expAmount, setExpAmount] = useState(0)
  const [expDate, setExpDate] = useState('')
  const [expNotes, setExpNotes] = useState('')

  // Activity form
  const [actTitle, setActTitle] = useState('')
  const [actDescription, setActDescription] = useState('')
  const [actLocation, setActLocation] = useState('')
  const [actStartTime, setActStartTime] = useState('')
  const [actEndTime, setActEndTime] = useState('')
  const [actEstCost, setActEstCost] = useState(0)

  async function handleActivate() {
    try {
      await activateMutation.mutateAsync(itineraryId)
      toast.success('Itinerary activated!')
    } catch {
      toast.error('Failed to activate itinerary')
    }
  }

  async function handleComplete() {
    try {
      await completeMutation.mutateAsync(itineraryId)
      toast.success('Itinerary marked as complete!')
    } catch {
      toast.error('Failed to complete itinerary')
    }
  }

  async function handleDelete() {
    try {
      await deleteMutation.mutateAsync(itineraryId)
      toast.success('Itinerary deleted')
      router.push('/itineraries')
    } catch {
      toast.error('Failed to delete itinerary')
    }
  }

  async function handleGenerateDays() {
    try {
      await generateDaysMutation.mutateAsync(itineraryId)
      toast.success('Days generated!')
    } catch {
      toast.error('Failed to generate days')
    }
  }

  async function handleDownloadPdf() {
    const supabase = createClient()
    const { data: { session } } = await supabase.auth.getSession()
    const url = getItineraryPdfUrl(itineraryId)
    const response = await fetch(url, {
      headers: session?.access_token ? { Authorization: `Bearer ${session.access_token}` } : {},
    })
    if (!response.ok) { toast.error('Failed to download PDF'); return }
    const blob = await response.blob()
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `itinerary-${id}.pdf`
    link.click()
  }

  async function handleAddExpense(e: React.FormEvent) {
    e.preventDefault()
    if (!expCategory || !expDescription || !expAmount || !expDate) {
      toast.error('All fields are required')
      return
    }
    try {
      const body: CreateExpenseRequest = {
        category: expCategory,
        description: expDescription,
        amount: expAmount,
        date: expDate,
        notes: expNotes || undefined,
      }
      await addExpenseMutation.mutateAsync({ itineraryId, data: body })
      setAddExpenseOpen(false)
      setExpCategory('')
      setExpDescription('')
      setExpAmount(0)
      setExpDate('')
      setExpNotes('')
      toast.success('Expense added')
    } catch {
      toast.error('Failed to add expense')
    }
  }

  async function handleDeleteExpense(expenseId: number) {
    try {
      await deleteExpenseMutation.mutateAsync({ itineraryId, expenseId })
      toast.success('Expense removed')
    } catch {
      toast.error('Failed to remove expense')
    }
  }

  function openAddActivity(dayId: number) {
    setSelectedDayId(dayId)
    setActTitle('')
    setActDescription('')
    setActLocation('')
    setActStartTime('')
    setActEndTime('')
    setActEstCost(0)
    setAddActivityOpen(true)
  }

  async function handleAddActivity(e: React.FormEvent) {
    e.preventDefault()
    if (!actTitle || !selectedDayId) {
      toast.error('Title is required')
      return
    }
    try {
      await addActivityMutation.mutateAsync({
        itineraryId,
        dayId: selectedDayId,
        data: {
          title: actTitle,
          description: actDescription || undefined,
          location: actLocation || undefined,
          startTime: actStartTime || undefined,
          endTime: actEndTime || undefined,
          estimatedCost: actEstCost || undefined,
          sortOrder: 0,
        },
      })
      setAddActivityOpen(false)
      toast.success('Activity added')
    } catch {
      toast.error('Failed to add activity')
    }
  }

  async function handleDeleteActivity(activityId: number) {
    try {
      await deleteActivityMutation.mutateAsync({ itineraryId, activityId })
      toast.success('Activity removed')
    } catch {
      toast.error('Failed to remove activity')
    }
  }

  if (isLoading) {
    return (
      <div className="space-y-6">
        <Skeleton className="h-8 w-64" />
        <Skeleton className="h-32 rounded-xl" />
        <Skeleton className="h-64 rounded-xl" />
      </div>
    )
  }

  if (!itin) {
    return (
      <div className="text-center py-16">
        <Map className="h-12 w-12 text-muted-foreground/30 mx-auto mb-4" />
        <p className="text-muted-foreground">Itinerary not found</p>
        <Link href="/itineraries"><Button variant="outline" className="mt-4">My Itineraries</Button></Link>
      </div>
    )
  }

  const dayCount = Math.ceil((new Date(itin.endDate).getTime() - new Date(itin.startDate).getTime()) / (1000 * 60 * 60 * 24)) + 1
  const actionLoading = activateMutation.isPending || completeMutation.isPending || deleteMutation.isPending

  return (
    <div className="space-y-6">
      <Link href="/itineraries">
        <Button variant="ghost" size="sm" className="gap-2 -ml-2">
          <ArrowLeft className="h-4 w-4" />
          My Itineraries
        </Button>
      </Link>

      {/* Header */}
      <Card className="bg-gradient-to-r from-primary/5 via-teal-50/50 to-emerald-50/30 border-0 shadow-sm">
        <CardContent className="py-5">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div>
              <div className="flex items-center gap-2 mb-1">
                <h1 className="font-display text-2xl font-bold text-foreground">{itin.title}</h1>
                <Badge variant="outline" className={`text-xs ${STATUS_COLORS[itin.status] ?? ''}`}>{itin.status}</Badge>
              </div>
              <div className="flex items-center gap-4 text-sm text-muted-foreground">
                <span className="flex items-center gap-1.5">
                  <Calendar className="h-4 w-4" />
                  {new Date(itin.startDate).toLocaleDateString()} – {new Date(itin.endDate).toLocaleDateString()}
                </span>
                <span className="flex items-center gap-1.5">
                  <Clock className="h-4 w-4" />
                  {dayCount} days
                </span>
              </div>
              {itin.description && <p className="text-sm text-muted-foreground mt-2">{itin.description}</p>}
            </div>
            <div className="flex items-center gap-2 flex-wrap">
              <Button variant="outline" size="sm" className="gap-1.5" onClick={handleDownloadPdf}>
                <Download className="h-4 w-4" />
                PDF
              </Button>
              {itin.status === 'PLANNING' && (
                <Button size="sm" className="gap-1.5" onClick={handleActivate} disabled={actionLoading}>
                  <Play className="h-4 w-4" />
                  Activate
                </Button>
              )}
              {itin.status === 'ACTIVE' && (
                <Button size="sm" variant="outline" className="gap-1.5" onClick={handleComplete} disabled={actionLoading}>
                  <CheckCircle2 className="h-4 w-4" />
                  Complete
                </Button>
              )}
              {(itin.status === 'PLANNING' || itin.status === 'CANCELLED') && (
                <AlertDialog>
                  <AlertDialogTrigger asChild>
                    <Button size="sm" variant="outline" className="gap-1.5 text-destructive border-destructive/20 hover:bg-destructive/5 hover:text-destructive">
                      <Trash2 className="h-4 w-4" />
                      Delete
                    </Button>
                  </AlertDialogTrigger>
                  <AlertDialogContent>
                    <AlertDialogHeader>
                      <AlertDialogTitle>Delete itinerary?</AlertDialogTitle>
                      <AlertDialogDescription>
                        This will permanently delete &quot;{itin.title}&quot; and all its data.
                      </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                      <AlertDialogCancel>Cancel</AlertDialogCancel>
                      <AlertDialogAction onClick={handleDelete} className="bg-destructive hover:bg-destructive/90">Delete</AlertDialogAction>
                    </AlertDialogFooter>
                  </AlertDialogContent>
                </AlertDialog>
              )}
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Tabs */}
      <Tabs defaultValue="schedule">
        <TabsList className="bg-white border shadow-sm">
          <TabsTrigger value="schedule" className="gap-2">
            <CalendarDays className="h-4 w-4" />
            Schedule
          </TabsTrigger>
          <TabsTrigger value="expenses" className="gap-2">
            <DollarSign className="h-4 w-4" />
            Expenses
          </TabsTrigger>
          <TabsTrigger value="summary" className="gap-2">
            <PieChart className="h-4 w-4" />
            Summary
          </TabsTrigger>
        </TabsList>

        {/* Schedule Tab */}
        <TabsContent value="schedule" className="mt-4">
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <h2 className="font-semibold">
                Day-by-Day Schedule
                {days && <span className="ml-2 text-sm font-normal text-muted-foreground">({days.length} days)</span>}
              </h2>
              {(!days || days.length === 0) && (
                <Button
                  size="sm"
                  className="gap-1.5"
                  onClick={handleGenerateDays}
                  disabled={generateDaysMutation.isPending}
                >
                  <Plus className="h-4 w-4" />
                  {generateDaysMutation.isPending ? 'Generating...' : 'Generate Days'}
                </Button>
              )}
            </div>

            {daysLoading ? (
              <div className="space-y-3">
                {[1, 2, 3].map((i) => <Skeleton key={i} className="h-24 rounded-xl" />)}
              </div>
            ) : !days || days.length === 0 ? (
              <Card className="border-dashed">
                <CardContent className="py-12 text-center">
                  <CalendarDays className="h-10 w-10 text-muted-foreground/30 mx-auto mb-3" />
                  <p className="text-muted-foreground text-sm">
                    No days generated yet. Click &quot;Generate Days&quot; to create day entries from your date range.
                  </p>
                </CardContent>
              </Card>
            ) : (
              <div className="space-y-4">
                {(days as ItineraryDay[]).map((day) => {
                  const Icon = ACTIVITY_ICONS['CUSTOM']
                  return (
                    <Card key={day.id} className="shadow-sm">
                      <CardHeader className="pb-2">
                        <div className="flex items-center justify-between">
                          <CardTitle className="text-base flex items-center gap-2">
                            <span className="h-7 w-7 rounded-full bg-primary/10 text-primary text-xs font-bold flex items-center justify-center">
                              {day.dayNumber}
                            </span>
                            Day {day.dayNumber}
                            <span className="text-sm font-normal text-muted-foreground">
                              — {new Date(day.date).toLocaleDateString(undefined, { weekday: 'long', month: 'short', day: 'numeric' })}
                            </span>
                          </CardTitle>
                          <Button
                            variant="outline"
                            size="sm"
                            className="gap-1 text-xs"
                            onClick={() => openAddActivity(day.id)}
                          >
                            <Plus className="h-3.5 w-3.5" />
                            Add Activity
                          </Button>
                        </div>
                        {day.notes && <p className="text-xs text-muted-foreground mt-1">{day.notes}</p>}
                      </CardHeader>
                      <CardContent className="pt-0">
                        {(!day.activities || day.activities.length === 0) ? (
                          <p className="text-xs text-muted-foreground py-2 text-center border border-dashed rounded-lg">
                            No activities planned for this day
                          </p>
                        ) : (
                          <div className="space-y-2">
                            {day.activities.map((act) => {
                              const ActIcon = ACTIVITY_ICONS[act.providerType ?? 'CUSTOM'] ?? MapPin
                              return (
                                <div key={act.id} className="flex items-start gap-3 p-2.5 rounded-lg bg-muted/50 group">
                                  <div className="h-8 w-8 rounded-lg bg-background flex items-center justify-center shrink-0 mt-0.5">
                                    <ActIcon className="h-4 w-4 text-primary" />
                                  </div>
                                  <div className="flex-1 min-w-0">
                                    <div className="flex items-center gap-2">
                                      <p className="text-sm font-medium truncate">{act.title}</p>
                                      {act.providerType && (
                                        <Badge variant="outline" className="text-[10px]">{act.providerType}</Badge>
                                      )}
                                    </div>
                                    <div className="flex items-center gap-3 text-xs text-muted-foreground mt-0.5">
                                      {(act.startTime || act.endTime) && (
                                        <span className="flex items-center gap-1">
                                          <Clock className="h-3 w-3" />
                                          {act.startTime ?? ''}{act.startTime && act.endTime ? ' – ' : ''}{act.endTime ?? ''}
                                        </span>
                                      )}
                                      {act.location && (
                                        <span className="flex items-center gap-1">
                                          <MapPin className="h-3 w-3" />
                                          {act.location}
                                        </span>
                                      )}
                                      {act.estimatedCost != null && act.estimatedCost > 0 && (
                                        <span>Rs. {act.estimatedCost.toLocaleString()}</span>
                                      )}
                                    </div>
                                    {act.description && <p className="text-xs text-muted-foreground mt-1">{act.description}</p>}
                                  </div>
                                  <Button
                                    variant="ghost"
                                    size="sm"
                                    className="h-7 w-7 p-0 opacity-0 group-hover:opacity-100 text-muted-foreground hover:text-destructive"
                                    onClick={() => handleDeleteActivity(act.id)}
                                  >
                                    <Trash2 className="h-3.5 w-3.5" />
                                  </Button>
                                </div>
                              )
                            })}
                          </div>
                        )}
                      </CardContent>
                    </Card>
                  )
                })}
              </div>
            )}
          </div>
        </TabsContent>

        {/* Expenses Tab */}
        <TabsContent value="expenses" className="mt-4">
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <h2 className="font-semibold">
                Expenses ({expenses.length})
                {summary && (
                  <span className="ml-2 text-sm font-normal text-muted-foreground">
                    Total: Rs {(summary as any).totalAmount?.toLocaleString() ?? (summary as any).totalSpent?.toLocaleString() ?? 0}
                  </span>
                )}
              </h2>
              <Button size="sm" className="gap-1.5" onClick={() => setAddExpenseOpen(true)}>
                <Plus className="h-4 w-4" />
                Add Expense
              </Button>
            </div>

            {expenses.length === 0 ? (
              <Card className="border-dashed">
                <CardContent className="py-12 text-center">
                  <DollarSign className="h-10 w-10 text-muted-foreground/30 mx-auto mb-3" />
                  <p className="text-muted-foreground text-sm">No expenses yet. Start tracking your spending.</p>
                </CardContent>
              </Card>
            ) : (
              <div className="space-y-2">
                {(expenses as any[]).map((exp: any) => (
                  <Card key={exp.id} className="shadow-sm">
                    <CardContent className="flex items-center gap-3 py-3">
                      <Badge variant="outline" className="text-xs shrink-0">{exp.category}</Badge>
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium truncate">{exp.description}</p>
                        <p className="text-xs text-muted-foreground">
                          {new Date(exp.date ?? exp.expenseDate).toLocaleDateString()}
                          {exp.notes ? ` · ${exp.notes}` : ''}
                        </p>
                      </div>
                      <span className="font-semibold text-sm shrink-0">Rs {exp.amount.toLocaleString()}</span>
                      <Button
                        variant="ghost"
                        size="sm"
                        className="h-7 w-7 p-0 text-muted-foreground hover:text-destructive"
                        onClick={() => handleDeleteExpense(exp.id)}
                      >
                        <Trash2 className="h-3.5 w-3.5" />
                      </Button>
                    </CardContent>
                  </Card>
                ))}
              </div>
            )}
          </div>
        </TabsContent>

        {/* Summary Tab */}
        <TabsContent value="summary" className="mt-4">
          {!summary || !((summary as any).totalAmount ?? (summary as any).totalSpent) ? (
            <Card className="border-dashed">
              <CardContent className="py-12 text-center">
                <PieChart className="h-10 w-10 text-muted-foreground/30 mx-auto mb-3" />
                <p className="text-muted-foreground text-sm">No expenses to summarize yet.</p>
              </CardContent>
            </Card>
          ) : (
            <div className="space-y-4">
              <Card className="shadow-sm">
                <CardHeader className="pb-3"><CardTitle className="text-base">Total Spending</CardTitle></CardHeader>
                <CardContent>
                  <p className="text-3xl font-bold text-foreground">
                    Rs {((summary as any).totalAmount ?? (summary as any).totalSpent ?? 0).toLocaleString()}
                  </p>
                  <p className="text-sm text-muted-foreground mt-1">{expenses.length} expenses tracked</p>
                  {itin.totalBudget && (
                    <div className="mt-3">
                      <div className="flex justify-between text-sm mb-1">
                        <span className="text-muted-foreground">Budget</span>
                        <span>Rs {itin.totalBudget.toLocaleString()}</span>
                      </div>
                      <div className="h-2 bg-muted rounded-full overflow-hidden">
                        <div
                          className={`h-full rounded-full ${
                            ((summary as any).totalAmount ?? (summary as any).totalSpent ?? 0) > itin.totalBudget
                              ? 'bg-red-500'
                              : 'bg-primary'
                          }`}
                          style={{
                            width: `${Math.min(100, (((summary as any).totalAmount ?? (summary as any).totalSpent ?? 0) / itin.totalBudget) * 100)}%`,
                          }}
                        />
                      </div>
                    </div>
                  )}
                </CardContent>
              </Card>

              {(summary as any).byCategory && Object.keys((summary as any).byCategory).length > 0 && (
                <Card className="shadow-sm">
                  <CardHeader className="pb-3"><CardTitle className="text-base">By Category</CardTitle></CardHeader>
                  <CardContent className="space-y-3">
                    {Object.entries((summary as any).byCategory as Record<string, number>)
                      .sort(([, a], [, b]) => b - a)
                      .map(([cat, amount]) => {
                        const total = (summary as any).totalAmount ?? (summary as any).totalSpent ?? 1
                        const pct = total > 0 ? (amount / total) * 100 : 0
                        return (
                          <div key={cat}>
                            <div className="flex items-center justify-between mb-1">
                              <span className="text-sm font-medium">{cat}</span>
                              <span className="text-sm text-muted-foreground">Rs {amount.toLocaleString()} ({pct.toFixed(0)}%)</span>
                            </div>
                            <div className="h-2 bg-muted rounded-full overflow-hidden">
                              <div className="h-full bg-primary rounded-full" style={{ width: `${pct}%` }} />
                            </div>
                          </div>
                        )
                      })}
                  </CardContent>
                </Card>
              )}
            </div>
          )}
        </TabsContent>
      </Tabs>

      {/* Add Expense Dialog */}
      <Dialog open={addExpenseOpen} onOpenChange={setAddExpenseOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Add Expense</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleAddExpense}>
            <div className="space-y-4 py-2">
              <div className="space-y-2">
                <Label>Category *</Label>
                <Select value={expCategory} onValueChange={setExpCategory} required>
                  <SelectTrigger className="h-11"><SelectValue placeholder="Select category" /></SelectTrigger>
                  <SelectContent>
                    {EXPENSE_CATEGORIES.map(c => (
                      <SelectItem key={c} value={c}>{c.charAt(0) + c.slice(1).toLowerCase()}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label>Description *</Label>
                <Input value={expDescription} onChange={e => setExpDescription(e.target.value)} placeholder="e.g. Hotel in Kandy" className="h-11" required />
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-2">
                  <Label>Amount (Rs.) *</Label>
                  <Input type="number" min={0} value={expAmount} onChange={e => setExpAmount(+e.target.value)} className="h-11" required />
                </div>
                <div className="space-y-2">
                  <Label>Date *</Label>
                  <Input type="date" value={expDate} onChange={e => setExpDate(e.target.value)} className="h-11" required />
                </div>
              </div>
              <div className="space-y-2">
                <Label>Notes</Label>
                <Input value={expNotes} onChange={e => setExpNotes(e.target.value)} placeholder="Optional notes..." className="h-11" />
              </div>
            </div>
            <DialogFooter className="mt-4">
              <Button type="button" variant="outline" onClick={() => setAddExpenseOpen(false)}>Cancel</Button>
              <Button type="submit" disabled={addExpenseMutation.isPending}>
                {addExpenseMutation.isPending ? 'Adding...' : 'Add Expense'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* Add Activity Dialog */}
      <Dialog open={addActivityOpen} onOpenChange={setAddActivityOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Add Activity</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleAddActivity}>
            <div className="space-y-4 py-2">
              <div className="space-y-2">
                <Label>Title *</Label>
                <Input value={actTitle} onChange={e => setActTitle(e.target.value)} placeholder="e.g. Visit Sigiriya Rock" className="h-11" required />
              </div>
              <div className="space-y-2">
                <Label>Description</Label>
                <Textarea value={actDescription} onChange={e => setActDescription(e.target.value)} placeholder="Details about this activity..." rows={2} />
              </div>
              <div className="space-y-2">
                <Label>Location</Label>
                <Input value={actLocation} onChange={e => setActLocation(e.target.value)} placeholder="e.g. Sigiriya, Central Province" className="h-11" />
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-2">
                  <Label>Start Time</Label>
                  <Input type="time" value={actStartTime} onChange={e => setActStartTime(e.target.value)} className="h-11" />
                </div>
                <div className="space-y-2">
                  <Label>End Time</Label>
                  <Input type="time" value={actEndTime} onChange={e => setActEndTime(e.target.value)} className="h-11" />
                </div>
              </div>
              <div className="space-y-2">
                <Label>Estimated Cost (Rs.)</Label>
                <Input type="number" min={0} value={actEstCost} onChange={e => setActEstCost(+e.target.value)} className="h-11" />
              </div>
            </div>
            <DialogFooter className="mt-4">
              <Button type="button" variant="outline" onClick={() => setAddActivityOpen(false)}>Cancel</Button>
              <Button type="submit" disabled={addActivityMutation.isPending}>
                {addActivityMutation.isPending ? 'Adding...' : 'Add Activity'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  )
}

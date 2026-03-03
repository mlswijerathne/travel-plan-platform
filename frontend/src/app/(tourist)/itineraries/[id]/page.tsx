'use client'

import { useEffect, useState } from 'react'
import { useParams, useRouter } from 'next/navigation'
import {
  getItinerary,
  activateItinerary,
  completeItinerary,
  deleteItinerary,
  getExpenses,
  getExpenseSummary,
  addExpense,
  deleteExpense,
  getItineraryPdfUrl,
} from '@/lib/api/itinerary'
import type { ItineraryDTO, ItineraryExpense, ExpenseSummary, CreateExpenseRequest } from '@/types/itinerary'
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
} from 'lucide-react'
import { toast } from 'sonner'
import Link from 'next/link'
import { createClient } from '@/lib/supabase/client'

const STATUS_COLORS: Record<string, string> = {
  PLANNING: 'bg-blue-50 text-blue-600 border-blue-200',
  ACTIVE: 'bg-emerald-50 text-emerald-600 border-emerald-200',
  COMPLETED: 'bg-gray-50 text-gray-500 border-gray-200',
  CANCELLED: 'bg-red-50 text-red-500 border-red-200',
}

export default function ItineraryDetailPage() {
  const { id } = useParams<{ id: string }>()
  const router = useRouter()
  const [itin, setItin] = useState<ItineraryDTO | null>(null)
  const [expenses, setExpenses] = useState<ItineraryExpense[]>([])
  const [summary, setSummary] = useState<ExpenseSummary | null>(null)
  const [loading, setLoading] = useState(true)
  const [actionLoading, setActionLoading] = useState(false)
  const [addExpenseOpen, setAddExpenseOpen] = useState(false)

  // Expense form
  const [expCategory, setExpCategory] = useState('')
  const [expDescription, setExpDescription] = useState('')
  const [expAmount, setExpAmount] = useState(0)
  const [expDate, setExpDate] = useState('')
  const [expNotes, setExpNotes] = useState('')
  const [savingExpense, setSavingExpense] = useState(false)

  useEffect(() => { load() }, [id])

  async function load() {
    try {
      const [itinData, expData, summaryData] = await Promise.allSettled([
        getItinerary(Number(id)),
        getExpenses(Number(id)),
        getExpenseSummary(Number(id)),
      ])
      if (itinData.status === 'fulfilled') setItin(itinData.value)
      if (expData.status === 'fulfilled') setExpenses(expData.value)
      if (summaryData.status === 'fulfilled') setSummary(summaryData.value)
    } catch {
      toast.error('Failed to load itinerary')
    } finally {
      setLoading(false)
    }
  }

  async function handleActivate() {
    setActionLoading(true)
    try {
      const updated = await activateItinerary(Number(id))
      setItin(updated)
      toast.success('Itinerary activated!')
    } catch {
      toast.error('Failed to activate itinerary')
    } finally {
      setActionLoading(false)
    }
  }

  async function handleComplete() {
    setActionLoading(true)
    try {
      const updated = await completeItinerary(Number(id))
      setItin(updated)
      toast.success('Itinerary marked as complete!')
    } catch {
      toast.error('Failed to complete itinerary')
    } finally {
      setActionLoading(false)
    }
  }

  async function handleDelete() {
    setActionLoading(true)
    try {
      await deleteItinerary(Number(id))
      toast.success('Itinerary deleted')
      router.push('/itineraries')
    } catch {
      toast.error('Failed to delete itinerary')
      setActionLoading(false)
    }
  }

  async function handleDownloadPdf() {
    const supabase = createClient()
    const { data: { session } } = await supabase.auth.getSession()
    const url = getItineraryPdfUrl(Number(id))
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
    setSavingExpense(true)
    try {
      const body: CreateExpenseRequest = {
        category: expCategory,
        description: expDescription,
        amount: expAmount,
        date: expDate,
        notes: expNotes || undefined,
      }
      const newExp = await addExpense(Number(id), body)
      setExpenses(prev => [...prev, newExp])
      // Refresh summary
      const newSummary = await getExpenseSummary(Number(id))
      setSummary(newSummary)
      setAddExpenseOpen(false)
      setExpCategory('')
      setExpDescription('')
      setExpAmount(0)
      setExpDate('')
      setExpNotes('')
      toast.success('Expense added')
    } catch {
      toast.error('Failed to add expense')
    } finally {
      setSavingExpense(false)
    }
  }

  async function handleDeleteExpense(expenseId: number) {
    try {
      await deleteExpense(Number(id), expenseId)
      setExpenses(prev => prev.filter(e => e.id !== expenseId))
      const newSummary = await getExpenseSummary(Number(id))
      setSummary(newSummary)
      toast.success('Expense removed')
    } catch {
      toast.error('Failed to remove expense')
    }
  }

  if (loading) {
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
      <Tabs defaultValue="expenses">
        <TabsList className="bg-white border shadow-sm">
          <TabsTrigger value="expenses" className="gap-2">
            <DollarSign className="h-4 w-4" />
            Expenses
          </TabsTrigger>
          <TabsTrigger value="summary" className="gap-2">
            <PieChart className="h-4 w-4" />
            Summary
          </TabsTrigger>
        </TabsList>

        {/* Expenses Tab */}
        <TabsContent value="expenses" className="mt-4">
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <h2 className="font-semibold">
                Expenses ({expenses.length})
                {summary && (
                  <span className="ml-2 text-sm font-normal text-muted-foreground">
                    Total: Rs {summary.totalAmount?.toLocaleString() ?? 0}
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
                {expenses.map(exp => (
                  <Card key={exp.id} className="shadow-sm">
                    <CardContent className="flex items-center gap-3 py-3">
                      <Badge variant="outline" className="text-xs shrink-0">{exp.category}</Badge>
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium truncate">{exp.description}</p>
                        <p className="text-xs text-muted-foreground">{new Date(exp.date).toLocaleDateString()}{exp.notes ? ` · ${exp.notes}` : ''}</p>
                      </div>
                      <span className="font-semibold text-sm shrink-0">Rs {exp.amount.toLocaleString()}</span>
                      <Button variant="ghost" size="sm" className="h-7 w-7 p-0 text-muted-foreground hover:text-destructive" onClick={() => handleDeleteExpense(exp.id)}>
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
          {!summary || !summary.totalAmount ? (
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
                  <p className="text-3xl font-bold text-foreground">Rs {summary.totalAmount.toLocaleString()}</p>
                  <p className="text-sm text-muted-foreground mt-1">{expenses.length} expenses tracked</p>
                </CardContent>
              </Card>

              {summary.byCategory && Object.keys(summary.byCategory).length > 0 && (
                <Card className="shadow-sm">
                  <CardHeader className="pb-3"><CardTitle className="text-base">By Category</CardTitle></CardHeader>
                  <CardContent className="space-y-3">
                    {Object.entries(summary.byCategory)
                      .sort(([, a], [, b]) => b - a)
                      .map(([cat, amount]) => {
                        const pct = summary.totalAmount > 0 ? (amount / summary.totalAmount) * 100 : 0
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
              <Button type="submit" disabled={savingExpense}>{savingExpense ? 'Adding...' : 'Add Expense'}</Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  )
}

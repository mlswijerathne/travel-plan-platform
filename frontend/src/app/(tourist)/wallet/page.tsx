'use client'

import { useEffect, useState } from 'react'
import { getTouristWallet } from '@/lib/api/tourist'
import type { WalletResponse } from '@/types/tourist'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import { formatCurrency, formatDate } from '@/lib/utils'
import { toast } from 'sonner'
import { Wallet, ArrowDownLeft, ArrowUpRight, RefreshCw, Receipt, CreditCard } from 'lucide-react'

export default function WalletPage() {
  const [wallet, setWallet] = useState<WalletResponse | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadWallet()
  }, [])

  async function loadWallet() {
    try {
      const res = await getTouristWallet()
      setWallet(res.data)
    } catch {
      toast.error('Failed to load wallet')
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="space-y-6">
        <Skeleton className="h-8 w-48" />
        <Skeleton className="h-48 w-full rounded-2xl" />
        <Skeleton className="h-64 w-full rounded-xl" />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="font-display text-2xl font-bold">Wallet</h1>
        <p className="text-muted-foreground mt-1">Manage your credits and view transaction history</p>
      </div>

      {/* Balance Card */}
      <Card className="overflow-hidden border-0 shadow-lg">
        <div className="bg-gradient-to-br from-primary via-teal-500 to-emerald-500 p-8 text-white relative">
          <div className="absolute top-0 right-0 w-64 h-64 bg-white/5 rounded-full -translate-y-1/2 translate-x-1/3" />
          <div className="absolute bottom-0 left-0 w-40 h-40 bg-white/5 rounded-full translate-y-1/2 -translate-x-1/4" />

          <div className="relative">
            <div className="flex items-center gap-2 mb-4">
              <div className="h-10 w-10 rounded-xl bg-white/20 flex items-center justify-center backdrop-blur-sm">
                <Wallet className="h-5 w-5" />
              </div>
              <div>
                <p className="text-sm font-medium text-white/70">Available Balance</p>
              </div>
            </div>
            <p className="text-4xl sm:text-5xl font-bold font-display tracking-tight">
              {formatCurrency(wallet?.balance ?? 0)}
            </p>
            <p className="text-sm text-white/60 mt-3">
              Credits from booking refunds and adjustments
            </p>
          </div>
        </div>
      </Card>

      {/* Quick Stats */}
      {wallet?.transactions && wallet.transactions.length > 0 && (
        <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
          <Card className="shadow-sm">
            <CardContent className="py-4 flex items-center gap-3">
              <div className="h-10 w-10 rounded-xl bg-emerald-50 flex items-center justify-center">
                <ArrowDownLeft className="h-5 w-5 text-emerald-600" />
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Total Refunds</p>
                <p className="text-lg font-bold font-display text-emerald-600">
                  {formatCurrency(
                    wallet.transactions
                      .filter(t => t.type === 'REFUND')
                      .reduce((sum, t) => sum + t.amount, 0)
                  )}
                </p>
              </div>
            </CardContent>
          </Card>
          <Card className="shadow-sm">
            <CardContent className="py-4 flex items-center gap-3">
              <div className="h-10 w-10 rounded-xl bg-orange-50 flex items-center justify-center">
                <ArrowUpRight className="h-5 w-5 text-orange-600" />
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Total Used</p>
                <p className="text-lg font-bold font-display text-orange-600">
                  {formatCurrency(
                    wallet.transactions
                      .filter(t => t.type === 'USED')
                      .reduce((sum, t) => sum + t.amount, 0)
                  )}
                </p>
              </div>
            </CardContent>
          </Card>
          <Card className="shadow-sm hidden sm:block">
            <CardContent className="py-4 flex items-center gap-3">
              <div className="h-10 w-10 rounded-xl bg-blue-50 flex items-center justify-center">
                <Receipt className="h-5 w-5 text-blue-600" />
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Transactions</p>
                <p className="text-lg font-bold font-display">{wallet.transactions.length}</p>
              </div>
            </CardContent>
          </Card>
        </div>
      )}

      {/* Transactions */}
      <Card className="shadow-sm">
        <CardHeader>
          <CardTitle className="font-display text-lg">Transaction History</CardTitle>
          <CardDescription>All wallet credits and debits</CardDescription>
        </CardHeader>
        <CardContent>
          {!wallet?.transactions.length ? (
            <div className="py-16 text-center space-y-4">
              <div className="mx-auto h-16 w-16 rounded-2xl bg-muted flex items-center justify-center">
                <CreditCard className="h-8 w-8 text-muted-foreground/40" />
              </div>
              <div>
                <p className="font-medium text-foreground">No transactions yet</p>
                <p className="text-sm text-muted-foreground mt-1">
                  Credits will appear here when you receive refunds from cancelled bookings.
                </p>
              </div>
            </div>
          ) : (
            <div className="space-y-2">
              {wallet.transactions.map((tx) => (
                <div
                  key={tx.id}
                  className="flex items-center justify-between p-3 rounded-xl hover:bg-muted/50 transition-colors"
                >
                  <div className="flex items-center gap-3">
                    <div className={`h-10 w-10 rounded-xl flex items-center justify-center ${
                      tx.type === 'REFUND'
                        ? 'bg-emerald-50'
                        : tx.type === 'USED'
                        ? 'bg-orange-50'
                        : 'bg-blue-50'
                    }`}>
                      {tx.type === 'REFUND' ? (
                        <ArrowDownLeft className="h-5 w-5 text-emerald-600" />
                      ) : tx.type === 'USED' ? (
                        <ArrowUpRight className="h-5 w-5 text-orange-600" />
                      ) : (
                        <RefreshCw className="h-5 w-5 text-blue-600" />
                      )}
                    </div>
                    <div>
                      <p className="text-sm font-medium">{tx.description}</p>
                      <div className="flex items-center gap-2 mt-0.5">
                        <Badge
                          variant="outline"
                          className={`text-[10px] px-1.5 py-0 ${
                            tx.type === 'REFUND'
                              ? 'text-emerald-600 border-emerald-200 bg-emerald-50'
                              : tx.type === 'USED'
                              ? 'text-orange-600 border-orange-200 bg-orange-50'
                              : 'text-blue-600 border-blue-200 bg-blue-50'
                          }`}
                        >
                          {tx.type}
                        </Badge>
                        <span className="text-xs text-muted-foreground">
                          {formatDate(tx.createdAt)}
                        </span>
                        {tx.referenceId && (
                          <span className="text-xs text-muted-foreground hidden sm:inline">
                            Ref: {tx.referenceId}
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                  <p className={`text-sm font-bold tabular-nums ${
                    tx.type === 'USED' ? 'text-orange-600' : 'text-emerald-600'
                  }`}>
                    {tx.type === 'USED' ? '-' : '+'}{formatCurrency(tx.amount)}
                  </p>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}

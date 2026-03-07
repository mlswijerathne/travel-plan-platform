'use client'

import { useEffect, useState } from 'react'
import { getProducts, deleteProduct } from '@/lib/api/products'
import type { ProductDTO } from '@/types/product'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { ShoppingBag, Trash2, Loader2, Plus } from 'lucide-react'
import Link from 'next/link'

export default function AdminProductsPage() {
  const [products, setProducts] = useState<ProductDTO[]>([])
  const [loading, setLoading] = useState(true)
  const [deletingId, setDeletingId] = useState<number | null>(null)

  const fetchProducts = () => {
    setLoading(true)
    getProducts()
      .then((data) => setProducts(data))
      .catch((err) => console.error('Failed to load products:', err))
      .finally(() => setLoading(false))
  }

  useEffect(() => { fetchProducts() }, [])

  const handleDelete = async (id: number) => {
    if (!confirm('Delete this product?')) return
    setDeletingId(id)
    try {
      await deleteProduct(id)
      setProducts((prev) => prev.filter((p) => p.id !== id))
    } catch (err) {
      console.error('Failed to delete product:', err)
    } finally {
      setDeletingId(null)
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Products</h1>
          <p className="text-muted-foreground">Manage shop products</p>
        </div>
        <Link href="/admin/products/new">
          <Button className="bg-emerald-600 hover:bg-emerald-700">
            <Plus className="mr-2 h-4 w-4" /> Add Product
          </Button>
        </Link>
      </div>

      {loading ? (
        <div className="flex items-center justify-center py-12">
          <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
        </div>
      ) : products.length === 0 ? (
        <div className="text-center py-12 border rounded-xl">
          <ShoppingBag className="h-12 w-12 mx-auto text-muted-foreground mb-3" />
          <p className="text-muted-foreground">No products yet</p>
        </div>
      ) : (
        <div className="border rounded-xl overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-muted/50">
              <tr>
                <th className="text-left px-4 py-3 font-medium">Product</th>
                <th className="text-left px-4 py-3 font-medium">Category</th>
                <th className="text-right px-4 py-3 font-medium">Price</th>
                <th className="text-right px-4 py-3 font-medium">Stock</th>
                <th className="text-center px-4 py-3 font-medium">Status</th>
                <th className="text-right px-4 py-3 font-medium">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y">
              {products.map((product) => (
                <tr key={product.id} className="hover:bg-accent/30 transition-colors">
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-3">
                      {product.imageUrl ? (
                        <img src={product.imageUrl} alt={product.name} className="h-10 w-10 rounded object-cover" />
                      ) : (
                        <div className="h-10 w-10 rounded bg-muted flex items-center justify-center">
                          <ShoppingBag className="h-4 w-4 text-muted-foreground" />
                        </div>
                      )}
                      <span className="font-medium">{product.name}</span>
                    </div>
                  </td>
                  <td className="px-4 py-3 text-muted-foreground">{product.category}</td>
                  <td className="px-4 py-3 text-right font-medium">Rs. {product.price}</td>
                  <td className="px-4 py-3 text-right">{product.stockQuantity}</td>
                  <td className="px-4 py-3 text-center">
                    <Badge variant={product.isActive ? 'default' : 'secondary'}>
                      {product.isActive ? 'Active' : 'Inactive'}
                    </Badge>
                  </td>
                  <td className="px-4 py-3 text-right">
                    <Button
                      variant="ghost"
                      size="icon"
                      className="h-8 w-8 text-destructive hover:text-destructive"
                      disabled={deletingId === product.id}
                      onClick={() => handleDelete(product.id)}
                    >
                      {deletingId === product.id ? (
                        <Loader2 className="h-4 w-4 animate-spin" />
                      ) : (
                        <Trash2 className="h-4 w-4" />
                      )}
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}

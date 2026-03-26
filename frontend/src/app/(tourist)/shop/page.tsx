"use client";

import { useEffect, useState } from "react";
import { Card, CardContent, CardFooter } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import Link from "next/link";
import { ShoppingBag } from "lucide-react";
import { getProducts } from "@/lib/api/products";
import type { ProductDTO } from "@/types/product";

export default function ShopPage() {
    const [products, setProducts] = useState<ProductDTO[]>([]);
    const [loading, setLoading] = useState(true);
    const [errorMsg, setErrorMsg] = useState("");

    useEffect(() => {
        getProducts()
            .then((data) => {
                setProducts(data);
                setLoading(false);
            })
            .catch((err) => {
                console.error("Failed to load shop:", err);
                setErrorMsg(err.message);
                setLoading(false);
            });
    }, []);

    return (
        <div className="space-y-8">
            {/* Hero Header Section */}
            <div className="bg-gradient-to-br from-primary via-primary to-tertiary text-white py-16 text-center rounded-2xl relative overflow-hidden">
                {/* Decorative circles */}
                <div className="absolute -top-10 -left-10 w-64 h-64 bg-white/5 rounded-full blur-3xl pointer-events-none" />
                <div className="absolute -bottom-10 -right-10 w-80 h-80 bg-white/5 rounded-full blur-3xl pointer-events-none" />
                <div className="relative z-10">
                    <h1 className="font-display text-4xl font-extrabold tracking-tight mb-2">Travel Shop</h1>
                    <p className="text-white/70 text-lg">Souvenirs, Gear &amp; Essentials</p>
                </div>
            </div>

            {/* Product Grid */}
            <div className="py-4">
                {loading ? (
                    <p className="text-center text-muted-foreground animate-pulse">Loading products...</p>
                ) : errorMsg ? (
                    <div className="text-center py-10 text-destructive">
                        <p className="font-bold text-lg">Error loading products!</p>
                        <p className="text-sm">{errorMsg}</p>
                    </div>
                ) : products.length === 0 ? (
                    <div className="text-center py-10">
                        <p className="text-muted-foreground text-lg">No items in the shop yet.</p>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
                        {products.map((item) => (
                            <Card key={item.id} className="overflow-hidden hover:-translate-y-1 hover:shadow-editorial-lg transition-all duration-300 bg-card rounded-xl shadow-editorial border border-border/30 flex flex-col h-full">

                                <div className="h-48 bg-surface-low relative group">
                                    {item.imageUrl ? (
                                        <img
                                            src={item.imageUrl}
                                            alt={item.name}
                                            className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
                                        />
                                    ) : (
                                        <div className="flex items-center justify-center h-full text-muted-foreground">No Image</div>
                                    )}
                                    <Badge className="absolute top-2 right-2 bg-primary/10 text-primary rounded-full hover:bg-primary/20">{item.category}</Badge>
                                </div>

                                <CardContent className="p-4 flex-grow">
                                    <h3 className="font-bold text-lg mb-1 text-foreground">{item.name}</h3>
                                    <p className="text-sm text-muted-foreground line-clamp-2 min-h-[40px]">{item.description}</p>

                                    <div className="mt-4 flex justify-between items-center">
                                        <span className="text-xl font-bold text-primary">Rs. {item.price}</span>
                                        <span className="text-xs text-muted-foreground font-medium">
                                            {item.stockQuantity > 0 ? `${item.stockQuantity} left` : "Out of Stock"}
                                        </span>
                                    </div>
                                </CardContent>

                                <CardFooter className="p-4 pt-0">
                                    {item.stockQuantity > 0 ? (
                                        <Link href={`/shop/buy/${item.id}`} className="w-full">
                                            <Button className="w-full bg-secondary hover:bg-secondary/90 text-white font-medium shadow-sm">
                                                <ShoppingBag className="mr-2 h-4 w-4" /> Buy Now
                                            </Button>
                                        </Link>
                                    ) : (
                                        <Button disabled className="w-full bg-surface-low text-muted-foreground cursor-not-allowed">
                                            Out of Stock
                                        </Button>
                                    )}
                                </CardFooter>
                            </Card>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}

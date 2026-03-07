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
        <div>
            {/* Header Section */}
            <div className="bg-emerald-900 text-white py-12 text-center rounded-xl">
                <h1 className="text-4xl font-bold mb-2">Travel Shop</h1>
                <p className="text-emerald-200">Souvenirs, Gear & Essentials</p>

            </div>

            {/* Product Grid */}
            <div className="container mx-auto px-4 py-12">
                {loading ? (
                    <p className="text-center text-gray-500 animate-pulse">Loading products...</p>
                ) : errorMsg ? (
                    <div className="text-center py-10 text-red-500">
                        <p className="font-bold text-lg">Error loading products!</p>
                        <p className="text-sm">{errorMsg}</p>
                    </div>
                ) : products.length === 0 ? (
                    <div className="text-center py-10">
                        <p className="text-gray-500 text-lg">No items in the shop yet.</p>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
                        {products.map((item) => (
                            <Card key={item.id} className="overflow-hidden hover:shadow-xl transition-all duration-300 border-none shadow-md bg-white flex flex-col h-full">

                                <div className="h-48 bg-gray-100 relative group">
                                    {item.imageUrl ? (
                                        <img
                                            src={item.imageUrl}
                                            alt={item.name}
                                            className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
                                        />
                                    ) : (
                                        <div className="flex items-center justify-center h-full text-gray-400">No Image</div>
                                    )}
                                    <Badge className="absolute top-2 right-2 bg-emerald-600 hover:bg-emerald-700">{item.category}</Badge>
                                </div>

                                <CardContent className="p-4 flex-grow">
                                    <h3 className="font-bold text-lg mb-1 text-gray-900">{item.name}</h3>
                                    <p className="text-sm text-gray-500 line-clamp-2 min-h-[40px]">{item.description}</p>

                                    <div className="mt-4 flex justify-between items-center">
                                        <span className="text-xl font-bold text-emerald-700">Rs. {item.price}</span>
                                        <span className="text-xs text-gray-400 font-medium">
                                            {item.stockQuantity > 0 ? `${item.stockQuantity} left` : "Out of Stock"}
                                        </span>
                                    </div>
                                </CardContent>

                                <CardFooter className="p-4 pt-0">
                                    {item.stockQuantity > 0 ? (
                                        <Link href={`/shop/buy/${item.id}`} className="w-full">
                                            <Button className="w-full bg-emerald-800 hover:bg-emerald-900 text-white font-medium shadow-sm">
                                                <ShoppingBag className="mr-2 h-4 w-4" /> Buy Now
                                            </Button>
                                        </Link>
                                    ) : (
                                        <Button disabled className="w-full bg-gray-100 text-gray-400 cursor-not-allowed">
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
"use client";

import Link from "next/link";
import { ShoppingBag, Search, Menu } from "lucide-react";
import { Button } from "@/components/ui/button";

export function Navbar() {
    return (
        <nav className="border-b bg-white shadow-sm sticky top-0 z-50">
            <div className="container mx-auto px-4 h-16 flex items-center justify-between">

                {/* Brand Logo */}
                <Link href="/shop" className="flex items-center gap-2">
                    <ShoppingBag className="h-6 w-6 text-emerald-700" />
                    <span className="text-xl font-bold text-gray-900">Travel<span className="text-emerald-700">Shop</span></span>
                </Link>

                {/* Desktop Navigation */}
                <div className="hidden md:flex items-center space-x-8">
                    <Link href="/shop" className="text-gray-600 hover:text-emerald-700 font-medium transition-colors">
                        All Products
                    </Link>
                    <Link href="/shop" className="text-gray-600 hover:text-emerald-700 font-medium transition-colors">
                        Souvenirs
                    </Link>
                    <Link href="/shop" className="text-gray-600 hover:text-emerald-700 font-medium transition-colors">
                        Travel Gear
                    </Link>
                </div>

                {/* Action Buttons */}
                <div className="flex items-center space-x-4">
                    <Button variant="ghost" size="icon" className="text-gray-600 hover:text-emerald-700">
                        <Search className="h-5 w-5" />
                    </Button>
                    <Link href="/shop/add">
                        <Button className="bg-emerald-700 hover:bg-emerald-800 text-white hidden md:flex">
                            + Add Product
                        </Button>
                    </Link>
                    <Button variant="ghost" size="icon" className="md:hidden text-gray-600">
                        <Menu className="h-6 w-6" />
                    </Button>
                </div>

            </div>
        </nav>
    );
}
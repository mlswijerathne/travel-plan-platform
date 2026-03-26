"use client";

import Link from "next/link";
import { ShoppingBag, Search, Menu } from "lucide-react";
import { Button } from "@/components/ui/button";

export function Navbar() {
    return (
        <nav className="bg-white/80 backdrop-blur-xl shadow-editorial sticky top-0 z-50">
            <div className="container mx-auto px-4 h-16 flex items-center justify-between">

                {/* Brand Logo */}
                <Link href="/shop" className="flex items-center gap-2">
                    <ShoppingBag className="h-6 w-6 text-primary" />
                    <span className="text-xl font-display font-extrabold text-foreground tracking-tight">Travel<span className="text-primary">Shop</span></span>
                </Link>

                {/* Desktop Navigation */}
                <div className="hidden md:flex items-center space-x-8">
                    <Link href="/shop" className="text-muted-foreground hover:text-primary font-medium transition-colors">
                        All Products
                    </Link>
                    <Link href="/shop" className="text-muted-foreground hover:text-primary font-medium transition-colors">
                        Souvenirs
                    </Link>
                    <Link href="/shop" className="text-muted-foreground hover:text-primary font-medium transition-colors">
                        Travel Gear
                    </Link>
                </div>

                {/* Action Buttons */}
                <div className="flex items-center space-x-4">
                    <Button variant="ghost" size="icon" className="text-muted-foreground hover:text-primary">
                        <Search className="h-5 w-5" />
                    </Button>
                    <Link href="/shop/add">
                        <Button className="bg-primary hover:bg-primary/90 text-white hidden md:flex">
                            + Add Product
                        </Button>
                    </Link>
                    <Button variant="ghost" size="icon" className="md:hidden text-muted-foreground">
                        <Menu className="h-6 w-6" />
                    </Button>
                </div>

            </div>
        </nav>
    );
}
import Link from "next/link";
import { Navbar } from "@/components/Navbar";
import { Button } from "@/components/ui/button";
import { ShoppingBag, Globe, ShieldCheck, HeartHandshake, ArrowRight } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";

export default function LandingPage() {
  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Navbar />

      {/* HERO SECTION - Fixed with inline background image */}
      <section className="relative bg-emerald-900 text-white overflow-hidden">
        <div
          className="absolute inset-0 bg-cover bg-center opacity-25"
          style={{ backgroundImage: "url('https://images.unsplash.com/photo-1580886577396-1996421145c0?q=80&w=2000&auto=format&fit=crop')" }}
        ></div>
        <div className="container mx-auto px-4 py-24 relative z-10 text-center">
          <Badge className="bg-emerald-700 mb-4 px-3 py-1 text-sm">Official Sri Lanka Travel Store</Badge>
          <h1 className="text-5xl md:text-7xl font-extrabold mb-6 tracking-tight">
            Take the Journey <br /> <span className="text-emerald-400">Home With You.</span>
          </h1>
          <p className="text-xl text-emerald-100 mb-10 max-w-2xl mx-auto leading-relaxed">
            Discover authentic Sri Lankan souvenirs, premium Ceylon tea, and essential travel gear.
            Directly supporting local artisans.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link href="/shop">
              <Button className="bg-white text-emerald-900 hover:bg-gray-100 h-14 px-8 text-lg font-bold rounded-full shadow-lg transition-transform hover:scale-105">
                <ShoppingBag className="mr-2 h-5 w-5" /> Shop Now
              </Button>
            </Link>
          </div>
        </div>
      </section>

      {/* CATEGORIES SECTION */}
      <section className="py-20 bg-white">
        <div className="container mx-auto px-4">
          <div className="text-center mb-12">
            <h2 className="text-3xl font-bold text-gray-900">Shop by Category</h2>
            <p className="text-gray-500 mt-2">Find exactly what you need for your trip or memories to take back.</p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-5xl mx-auto">
            {/* Category 1 */}
            <Link href="/shop" className="group">
              <Card className="overflow-hidden border-none shadow-md hover:shadow-xl transition-all duration-300 cursor-pointer h-64 relative">
                <div
                  className="absolute inset-0 bg-cover bg-center transition-transform duration-500 group-hover:scale-110"
                  style={{ backgroundImage: "url('https://images.unsplash.com/photo-1571781926291-c477ebef0dd9?q=80&w=800&auto=format&fit=crop')" }}
                ></div>
                <div className="absolute inset-0 bg-black/40 group-hover:bg-black/50 transition-colors"></div>
                <CardContent className="absolute inset-0 flex flex-col items-center justify-center text-white">
                  <h3 className="text-2xl font-bold mb-2">Authentic Souvenirs</h3>
                  <span className="flex items-center text-sm font-medium">Browse <ArrowRight className="ml-1 h-4 w-4" /></span>
                </CardContent>
              </Card>
            </Link>

            {/* Category 2 */}
            <Link href="/shop" className="group">
              <Card className="overflow-hidden border-none shadow-md hover:shadow-xl transition-all duration-300 cursor-pointer h-64 relative">
                <div
                  className="absolute inset-0 bg-cover bg-center transition-transform duration-500 group-hover:scale-110"
                  style={{ backgroundImage: "url('https://images.unsplash.com/photo-1563822249548-9a72b6353cd1?q=80&w=800&auto=format&fit=crop')" }}
                ></div>
                <div className="absolute inset-0 bg-black/40 group-hover:bg-black/50 transition-colors"></div>
                <CardContent className="absolute inset-0 flex flex-col items-center justify-center text-white">
                  <h3 className="text-2xl font-bold mb-2">Ceylon Tea</h3>
                  <span className="flex items-center text-sm font-medium">Browse <ArrowRight className="ml-1 h-4 w-4" /></span>
                </CardContent>
              </Card>
            </Link>

            {/* Category 3 */}
            <Link href="/shop" className="group">
              <Card className="overflow-hidden border-none shadow-md hover:shadow-xl transition-all duration-300 cursor-pointer h-64 relative">
                <div
                  className="absolute inset-0 bg-cover bg-center transition-transform duration-500 group-hover:scale-110"
                  style={{ backgroundImage: "url('https://images.unsplash.com/photo-1553531384-cc64ac80f931?q=80&w=800&auto=format&fit=crop')" }}
                ></div>
                <div className="absolute inset-0 bg-black/40 group-hover:bg-black/50 transition-colors"></div>
                <CardContent className="absolute inset-0 flex flex-col items-center justify-center text-white">
                  <h3 className="text-2xl font-bold mb-2">Travel Gear</h3>
                  <span className="flex items-center text-sm font-medium">Browse <ArrowRight className="ml-1 h-4 w-4" /></span>
                </CardContent>
              </Card>
            </Link>
          </div>
        </div>
      </section>

      {/* WHY CHOOSE US SECTION */}
      <section className="py-20 bg-emerald-50">
        <div className="container mx-auto px-4 max-w-5xl">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 text-center">
            <div className="flex flex-col items-center p-6">
              <div className="h-16 w-16 bg-emerald-100 text-emerald-700 rounded-full flex items-center justify-center mb-6">
                <Globe className="h-8 w-8" />
              </div>
              <h3 className="text-xl font-bold text-gray-900 mb-2">Island-Wide Delivery</h3>
              <p className="text-gray-600">Get your items delivered directly to your hotel before you even arrive.</p>
            </div>

            <div className="flex flex-col items-center p-6">
              <div className="h-16 w-16 bg-emerald-100 text-emerald-700 rounded-full flex items-center justify-center mb-6">
                <ShieldCheck className="h-8 w-8" />
              </div>
              <h3 className="text-xl font-bold text-gray-900 mb-2">Secure Payments</h3>
              <p className="text-gray-600">Your transactions are protected by industry-leading security standards.</p>
            </div>

            <div className="flex flex-col items-center p-6">
              <div className="h-16 w-16 bg-emerald-100 text-emerald-700 rounded-full flex items-center justify-center mb-6">
                <HeartHandshake className="h-8 w-8" />
              </div>
              <h3 className="text-xl font-bold text-gray-900 mb-2">Support Locals</h3>
              <p className="text-gray-600">Every purchase directly supports local Sri Lankan artisans and businesses.</p>
            </div>
          </div>
        </div>
      </section>

      {/* CTA SECTION */}
      <section className="bg-emerald-900 text-white py-16">
        <div className="container mx-auto px-4 text-center">
          <h2 className="text-3xl font-bold mb-4">Ready to start exploring?</h2>
          <p className="text-emerald-200 mb-8 max-w-xl mx-auto">
            Browse our full catalog of over 500 authentic products and travel essentials.
          </p>
          <Link href="/shop">
            <Button className="bg-white text-emerald-900 hover:bg-gray-100 h-12 px-8 font-bold">
              View All Products
            </Button>
          </Link>
        </div>
      </section>


    </div>
  );
}

// Quick component for the Badge used in the hero section
function Badge({ children, className }: { children: React.ReactNode, className?: string }) {
  return (
    <span className={`inline-block rounded-full text-xs font-semibold tracking-wide uppercase ${className}`}>
      {children}
    </span>
  );
}
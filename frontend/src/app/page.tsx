import Link from "next/link";
import { Navbar } from "@/components/Navbar";
import { Button } from "@/components/ui/button";
import { ArrowRight, MapPin, Calendar, ShieldCheck } from "lucide-react";

export default function Home() {
  return (
    <div className="min-h-screen flex flex-col">
      <Navbar />

      {/* HERO SECTION */}
      <section className="flex-1 bg-black text-white flex flex-col justify-center items-center text-center px-4 py-20">
        <h1 className="text-5xl md:text-7xl font-extrabold tracking-tight mb-6">
          Drive Your <span className="text-blue-500">Dreams.</span>
        </h1>
        <p className="text-xl text-gray-400 max-w-2xl mb-10">
          Premium car rentals at unbeatable prices. From city commuters to luxury cruisers,
          find the perfect vehicle for your next journey in Sri Lanka.
        </p>
        <div className="flex gap-4">
          <Link href="/vehicles">
            <Button size="lg" className="h-14 px-8 text-lg bg-blue-600 hover:bg-blue-700">
              Browse Vehicles <ArrowRight className="ml-2" />
            </Button>
          </Link>
          <Link href="/about">
            <Button size="lg" variant="outline" className="h-14 px-8 text-lg text-black bg-white hover:bg-gray-200">
              Learn More
            </Button>
          </Link>
        </div>
      </section>

      {/* FEATURES SECTION */}
      <section className="py-20 bg-gray-50">
        <div className="container mx-auto px-4 grid grid-cols-1 md:grid-cols-3 gap-8">

          <div className="p-8 bg-white rounded-xl shadow-sm border hover:shadow-md transition">
            <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center text-blue-600 mb-4">
              <MapPin size={24} />
            </div>
            <h3 className="text-xl font-bold mb-2">Island-wide Delivery</h3>
            <p className="text-gray-500">We deliver your chosen vehicle directly to your location, airport, or hotel anywhere in the country.</p>
          </div>

          <div className="p-8 bg-white rounded-xl shadow-sm border hover:shadow-md transition">
            <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center text-green-600 mb-4">
              <ShieldCheck size={24} />
            </div>
            <h3 className="text-xl font-bold mb-2">Secure & Insured</h3>
            <p className="text-gray-500">Drive with peace of mind. All our vehicles come with comprehensive insurance and 24/7 roadside support.</p>
          </div>

          <div className="p-8 bg-white rounded-xl shadow-sm border hover:shadow-md transition">
            <div className="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center text-purple-600 mb-4">
              <Calendar size={24} />
            </div>
            <h3 className="text-xl font-bold mb-2">Flexible Bookings</h3>
            <p className="text-gray-500">Need a car for a day, a week, or a month? Our flexible booking plans adapt to your schedule.</p>
          </div>

        </div>
      </section>

      {/* CALL TO ACTION */}
      <section className="py-20 bg-white border-t text-center">
        <h2 className="text-3xl font-bold mb-4">Ready to hit the road?</h2>
        <p className="text-gray-600 mb-8">Join thousands of satisfied customers today.</p>
        <Link href="/vehicles/add">
          <Button variant="outline" size="lg">List Your Car</Button>
        </Link>
      </section>

      {/* FOOTER */}
      <footer className="bg-black text-gray-400 py-8 text-center text-sm">
        <p>&copy; 2026 TravelPlan Platform. All rights reserved.</p>
      </footer>
    </div>
  );
}
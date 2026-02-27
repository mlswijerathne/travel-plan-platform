"use client";

import React, { useState, useEffect } from "react";
import Link from "next/link";
import { Search, MapPin, DollarSign, Clock, ChevronRight, Loader2, Frown } from "lucide-react";
import { TripPackage } from "@/types/trip-package";

export default function PackagesPage() {
    const [packages, setPackages] = useState<TripPackage[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // Filter States
    const [searchTerm, setSearchTerm] = useState("");
    const [destination, setDestination] = useState("");
    const [maxPrice, setMaxPrice] = useState("");

    // Function to fetch data based on current filters
    const fetchPackages = async () => {
        setLoading(true);
        setError(null);

        try {
            // Build the query string based on the API spec
            const params = new URLSearchParams();
            if (searchTerm) params.append("query", searchTerm);
            if (destination) params.append("destination", destination);
            if (maxPrice) params.append("maxPrice", maxPrice);

            // Add default pagination if needed
            params.append("page", "0");
            params.append("size", "20");

            const res = await fetch(`http://localhost:8089/api/packages?${params.toString()}`);
            if (!res.ok) throw new Error("Failed to fetch packages");

            const json = await res.json();
            // The spec shows the array is inside a "data" property
            setPackages(json.data || []);
        } catch (err: any) {
            console.error(err);
            setError("Could not load travel packages from the server.");
        } finally {
            setLoading(false);
        }
    };

    // Fetch all packages on initial load
    useEffect(() => {
        fetchPackages();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    // Handle form submission to apply filters
    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        fetchPackages();
    };

    // Handle clearing filters
    const handleClearFilters = () => {
        setSearchTerm("");
        setDestination("");
        setMaxPrice("");
        // We use a small timeout to ensure state clears before fetching
        setTimeout(() => {
            fetchPackages();
        }, 0);
    };

    return (
        <div className="min-h-screen bg-gray-50 pb-20">
            {/* Header Section */}
            <div className="bg-emerald-900 py-16 px-4 text-center">
                <h1 className="text-4xl md:text-5xl font-extrabold text-white mb-4">Explore Our Travel Packages</h1>
                <p className="text-emerald-100 text-lg max-w-2xl mx-auto mb-8">
                    Discover hand-picked itineraries, from cultural triangles to southern beaches.
                </p>

                {/* Search & Filter Bar */}
                <div className="max-w-5xl mx-auto bg-white rounded-xl shadow-lg p-2 md:p-4">
                    <form onSubmit={handleSearch} className="flex flex-col md:flex-row gap-3">
                        {/* Keyword Search */}
                        <div className="flex-1 relative">
                            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                            <input
                                type="text"
                                placeholder="Search packages (e.g., Adventure, Heritage)"
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                                className="w-full pl-10 pr-4 py-3 rounded-lg border border-gray-200 focus:ring-2 focus:ring-emerald-500 outline-none"
                            />
                        </div>

                        {/* Destination Filter */}
                        <div className="flex-1 relative">
                            <MapPin className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                            <input
                                type="text"
                                placeholder="Destination (e.g., Galle, Ella)"
                                value={destination}
                                onChange={(e) => setDestination(e.target.value)}
                                className="w-full pl-10 pr-4 py-3 rounded-lg border border-gray-200 focus:ring-2 focus:ring-emerald-500 outline-none"
                            />
                        </div>

                        {/* Max Price Filter */}
                        <div className="flex-1 relative">
                            <DollarSign className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                            <input
                                type="number"
                                placeholder="Max Price (Rs.)"
                                value={maxPrice}
                                onChange={(e) => setMaxPrice(e.target.value)}
                                className="w-full pl-10 pr-4 py-3 rounded-lg border border-gray-200 focus:ring-2 focus:ring-emerald-500 outline-none"
                            />
                        </div>

                        <button
                            type="submit"
                            className="bg-emerald-600 hover:bg-emerald-700 text-white font-bold py-3 px-8 rounded-lg transition-colors flex items-center justify-center whitespace-nowrap"
                        >
                            Search
                        </button>
                    </form>

                    {/* Active Filters / Clear Button */}
                    {(searchTerm || destination || maxPrice) && (
                        <div className="mt-4 flex justify-end">
                            <button onClick={handleClearFilters} className="text-sm text-gray-500 hover:text-red-500 transition-colors underline">
                                Clear all filters
                            </button>
                        </div>
                    )}
                </div>
            </div>

            {/* Main Content Area */}
            <div className="container mx-auto px-4 mt-12">
                {loading ? (
                    <div className="flex justify-center items-center py-20">
                        <Loader2 className="w-12 h-12 animate-spin text-emerald-600" />
                    </div>
                ) : error ? (
                    <div className="text-center py-20 text-red-600 font-medium">
                        <Frown className="w-12 h-12 mx-auto mb-4 text-red-400" />
                        {error}
                    </div>
                ) : packages.length === 0 ? (
                    <div className="text-center py-20 bg-white rounded-xl border border-gray-200">
                        <Search className="w-12 h-12 mx-auto mb-4 text-gray-300" />
                        <h3 className="text-xl font-bold text-gray-800 mb-2">No packages found</h3>
                        <p className="text-gray-500">Try adjusting your search criteria or clearing your filters.</p>
                        <button onClick={handleClearFilters} className="mt-4 text-emerald-600 font-semibold hover:underline">
                            View all packages
                        </button>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-8">
                        {packages.map((pkg) => {
                            const finalPrice = pkg.basePrice - (pkg.basePrice * (pkg.discountPercentage / 100));

                            return (
                                <Link href={`/packages/${pkg.id}`} key={pkg.id}>
                                    <div className="bg-white rounded-2xl shadow-sm hover:shadow-xl transition-all duration-300 border border-gray-100 overflow-hidden cursor-pointer h-full flex flex-col group">

                                        {/* Image Section */}
                                        <div className="relative h-56 bg-gray-200 overflow-hidden">
                                            {pkg.images && pkg.images.length > 0 ? (
                                                <img
                                                    src={pkg.images[0]}
                                                    alt={pkg.name}
                                                    className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                                                />
                                            ) : (
                                                <div className="w-full h-full flex items-center justify-center text-gray-400 bg-gray-100">No Image</div>
                                            )}

                                            {/* Discount Badge */}
                                            {pkg.discountPercentage > 0 && (
                                                <div className="absolute top-4 right-4 bg-red-500 text-white text-xs font-bold px-3 py-1.5 rounded-full shadow-md">
                                                    {pkg.discountPercentage}% OFF
                                                </div>
                                            )}
                                        </div>

                                        {/* Content Section */}
                                        <div className="p-6 grow flex flex-col">
                                            <h3 className="text-xl font-bold text-gray-900 mb-3 line-clamp-1">{pkg.name}</h3>

                                            <div className="flex items-center text-sm text-gray-500 mb-4 space-x-4">
                                                <span className="flex items-center"><Clock className="w-4 h-4 mr-1 text-emerald-600" /> {pkg.durationDays} Days</span>
                                                {pkg.destinations && pkg.destinations.length > 0 && (
                                                    <span className="flex items-center"><MapPin className="w-4 h-4 mr-1 text-emerald-600" /> {pkg.destinations.length} Places</span>
                                                )}
                                            </div>

                                            {/* Destination Badges */}
                                            <div className="flex flex-wrap gap-2 mb-6">
                                                {pkg.destinations?.slice(0, 3).map((dest, i) => (
                                                    <span key={i} className="bg-blue-50 text-blue-600 border border-blue-100 text-xs px-3 py-1 rounded-full font-medium">
                                                        {dest}
                                                    </span>
                                                ))}
                                                {pkg.destinations && pkg.destinations.length > 3 && (
                                                    <span className="bg-gray-50 text-gray-500 border border-gray-200 text-xs px-2 py-1 rounded-full">
                                                        +{pkg.destinations.length - 3}
                                                    </span>
                                                )}
                                            </div>

                                            <div className="mt-auto pt-4 border-t border-gray-100 flex items-end justify-between">
                                                <div>
                                                    <p className="text-xs font-bold text-gray-400 uppercase tracking-wider mb-1">Starting From</p>
                                                    <div className="flex items-baseline space-x-2">
                                                        <span className="text-2xl font-black text-gray-900">Rs {finalPrice.toFixed(2)}</span>
                                                        {pkg.discountPercentage > 0 && (
                                                            <span className="text-sm text-gray-400 line-through">Rs {pkg.basePrice.toFixed(2)}</span>
                                                        )}
                                                    </div>
                                                </div>
                                                <div className="bg-blue-50 p-2 rounded-full text-blue-600 group-hover:bg-blue-600 group-hover:text-white transition-colors">
                                                    <ChevronRight className="w-5 h-5" />
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </Link>
                            );
                        })}
                    </div>
                )}
            </div>
        </div>
    );
}
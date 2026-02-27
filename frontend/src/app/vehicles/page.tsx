"use client";

import React, { useState, useEffect, useCallback } from 'react';
import { Navbar } from '@/components/Navbar';
import { VehicleCard } from '@/components/VehicleCard';
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Search, X } from "lucide-react";
// Optional: If you want to use your SearchBar component, you can import it here
// import { SearchBar } from '@/components/SearchBar';

interface Vehicle {
    id: number;
    imageUrl?: string;   // legacy field
    images?: string[];   // new field from API
    make: string;
    model: string;
    vehicleType: string;
    year: number;
    seatingCapacity: number;
    dailyRate: number;
    isAvailable: boolean;
}

export default function VehiclesPage() {
    const [vehicles, setVehicles] = useState<Vehicle[]>([]);
    const [loading, setLoading] = useState(true);

    // Search Filters State
    const [filters, setFilters] = useState({
        vehicleType: "ALL",
        minPrice: "",
        maxPrice: "",
        capacity: "",
        query: "" // Added to support your SearchBar component if integrated
    });

    // Function to Fetch Data from Java
    const fetchVehicles = useCallback(async () => {
        setLoading(true);
        try {
            // Build the Query URL with updated parameter names
            const params = new URLSearchParams();
            if (filters.vehicleType && filters.vehicleType !== "ALL") params.append("vehicleType", filters.vehicleType);
            if (filters.minPrice) params.append("minDailyRate", filters.minPrice);
            if (filters.maxPrice) params.append("maxDailyRate", filters.maxPrice);
            if (filters.capacity) params.append("minCapacity", filters.capacity);
            if (filters.query) params.append("query", filters.query);
            params.append("size", "100"); // fetch up to 100 vehicles

            const endpoint = `http://localhost:8085/api/vehicles?${params.toString()}`;

            const res = await fetch(endpoint);
            if (res.ok) {
                const pageData = await res.json();
                // API returns a Spring Page object, extract the content array
                setVehicles(pageData.content ?? pageData);
            }
        } catch (error) {
            console.error("Failed to fetch vehicles", error);
        } finally {
            setLoading(false);
        }
    }, [filters]);

    // Initial Load & Refetch when filters change
    useEffect(() => {
        // Debounce: Wait 500ms after user stops typing to save server calls
        const timer = setTimeout(() => {
            fetchVehicles();
        }, 500);
        return () => clearTimeout(timer);
    }, [fetchVehicles]);

    // Handle Input Changes
    const handleFilterChange = (key: string, value: string) => {
        setFilters(prev => ({ ...prev, [key]: value }));
    };

    const clearFilters = () => {
        setFilters({ vehicleType: "ALL", minPrice: "", maxPrice: "", capacity: "", query: "" });
    };

    return (
        <div className="min-h-screen bg-gray-50">
            <Navbar />
            <main className="container mx-auto px-4 py-8">

                {/* HEADER */}
                <div className="mb-8 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
                    <div>
                        <h1 className="text-3xl font-bold text-gray-900">Find Your Perfect Drive</h1>
                        <p className="text-gray-600 mt-2">Browse our extensive fleet of premium vehicles.</p>
                    </div>
                    {/* If you want to use the SearchBar component, you can place it here */}
                    {/* <SearchBar onSearch={(val) => handleFilterChange("query", val)} /> */}
                </div>

                {/* SEARCH & FILTER BAR */}
                <div className="bg-white p-4 rounded-xl shadow-sm border mb-8 grid grid-cols-1 md:grid-cols-5 gap-4 items-end">

                    {/* 1. Type Filter */}
                    <div className="space-y-2 relative">
                        <span className="text-sm font-medium">Vehicle Type</span>
                        <Select value={filters.vehicleType} onValueChange={(val) => handleFilterChange("vehicleType", val)}>
                            <SelectTrigger className="bg-white">
                                <SelectValue placeholder="All Types" />
                            </SelectTrigger>
                            {/* FIX: Added bg-white and z-50 to stop it from being transparent */}
                            <SelectContent className="bg-white z-50 shadow-lg border">
                                <SelectItem value="ALL">All Types</SelectItem>
                                {/* FIX: Values must be UPPERCASE to match backend validation */}
                                <SelectItem value="CAR">Car</SelectItem>
                                <SelectItem value="SUV">SUV</SelectItem>
                                <SelectItem value="VAN">Van</SelectItem>
                                <SelectItem value="BUS">Bus</SelectItem>
                                <SelectItem value="TUK_TUK">Tuk-Tuk</SelectItem>
                                <SelectItem value="MOTORBIKE">Motorbike</SelectItem>
                            </SelectContent>
                        </Select>
                    </div>

                    {/* 2. Min Price */}
                    <div className="space-y-2">
                        <span className="text-sm font-medium">Min Price (Rs)</span>
                        <Input
                            type="number"
                            placeholder="0"
                            value={filters.minPrice}
                            onChange={(e) => handleFilterChange("minPrice", e.target.value)}
                        />
                    </div>

                    {/* 3. Max Price */}
                    <div className="space-y-2">
                        <span className="text-sm font-medium">Max Price (Rs)</span>
                        <Input
                            type="number"
                            placeholder="500"
                            value={filters.maxPrice}
                            onChange={(e) => handleFilterChange("maxPrice", e.target.value)}
                        />
                    </div>

                    {/* 4. Capacity */}
                    <div className="space-y-2">
                        <span className="text-sm font-medium">Seats</span>
                        <Input
                            type="number"
                            placeholder="Any"
                            value={filters.capacity}
                            onChange={(e) => handleFilterChange("capacity", e.target.value)}
                        />
                    </div>

                    {/* 5. Clear Button */}
                    <Button variant="outline" onClick={clearFilters} className="w-full bg-white hover:bg-gray-100">
                        <X size={16} className="mr-2" /> Clear Filters
                    </Button>
                </div>

                {/* VEHICLE GRID */}
                {loading ? (
                    <div className="text-center py-20 text-gray-500 flex flex-col items-center">
                        <Search className="animate-pulse mb-4" size={48} />
                        <p>Searching Fleet...</p>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                        {vehicles.length > 0 ? (
                            vehicles.map((vehicle) => (
                                <VehicleCard
                                    key={vehicle.id}
                                    id={vehicle.id}
                                    imageUrl={(vehicle.images && vehicle.images[0]) || vehicle.imageUrl}
                                    make={vehicle.make}
                                    model={vehicle.model}
                                    type={vehicle.vehicleType}
                                    year={vehicle.year}
                                    seats={vehicle.seatingCapacity}
                                    price={vehicle.dailyRate}
                                    available={vehicle.isAvailable}
                                />
                            ))
                        ) : (
                            <div className="col-span-full text-center py-20 bg-white rounded-xl border border-dashed">
                                <p className="text-gray-500 text-lg">No vehicles match your search.</p>
                                <Button variant="link" onClick={clearFilters}>Clear filters to see all cars</Button>
                            </div>
                        )}
                    </div>
                )}
            </main>
        </div>
    );
}
"use client";

import React, { useState, useEffect } from "react";
import { useRouter, useParams } from "next/navigation";
import { Navbar } from "@/components/Navbar";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Checkbox } from "@/components/ui/checkbox";

export default function EditVehiclePage() {
    const router = useRouter();
    const params = useParams(); // Gets the ID from the URL
    const vehicleId = params.id;

    const [loading, setLoading] = useState(false);
    const [fetching, setFetching] = useState(true);

    // FIX: State now matches ALL required fields for the backend DTO
    const [formData, setFormData] = useState({
        make: "",
        model: "",
        vehicleType: "CAR",
        year: 2024,
        licensePlate: "",
        seatingCapacity: 4,
        dailyRate: "",
        features: [] as string[],
        images: [] as string[],
        isAvailable: true
    });

    // 1. Fetch Existing Data When Page Loads
    useEffect(() => {
        async function fetchVehicle() {
            try {
                const res = await fetch(`http://localhost:8085/api/vehicles/${vehicleId}`);
                if (res.ok) {
                    const data = await res.json();
                    setFormData({
                        make: data.make || "",
                        model: data.model || "",
                        vehicleType: data.vehicleType || "CAR",
                        year: data.year || 2024,
                        licensePlate: data.licensePlate || "",
                        seatingCapacity: data.seatingCapacity || 4,
                        dailyRate: data.dailyRate || "",
                        features: data.features || [],
                        // Safely handle the images array or fallback to legacy imageUrl
                        images: data.images?.length > 0 ? data.images : (data.imageUrl ? [data.imageUrl] : []),
                        isAvailable: data.isAvailable !== false // default to true if null
                    });
                } else {
                    alert("Vehicle not found!");
                    router.push("/admin");
                }
            } catch (error) {
                console.error("Error fetching vehicle:", error);
            } finally {
                setFetching(false);
            }
        }
        fetchVehicle();
    }, [vehicleId, router]);

    // 2. Handle Typing
    const handleChange = (e: any) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    // 3. Submit the Update (PUT Request)
    const handleSubmit = async (e: any) => {
        e.preventDefault();
        setLoading(true);

        try {
            // FIX: Format payload to match the DTO types
            const payload = {
                ...formData,
                year: Number(formData.year),
                seatingCapacity: Number(formData.seatingCapacity),
                dailyRate: Number(formData.dailyRate),
            };

            const response = await fetch(`http://localhost:8085/api/vehicles/${vehicleId}`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    "X-User-Id": "admin" // Pass admin context
                },
                body: JSON.stringify(payload),
            });

            if (response.ok) {
                alert("Vehicle Updated Successfully!");
                router.push("/admin");
            } else {
                alert("Failed to update vehicle.");
            }
        } catch (error) {
            console.error("Error:", error);
            alert("Something went wrong!");
        } finally {
            setLoading(false);
        }
    };

    if (fetching) return <div className="text-center py-20 mt-10 text-xl">Loading Vehicle Details...</div>;

    // Grab the first image to display in the preview
    const displayImage = formData.images.length > 0 ? formData.images[0] : "";

    return (
        <div className="min-h-screen bg-gray-50 pb-20">
            <Navbar />
            <div className="container mx-auto px-4 py-12 flex justify-center">
                <Card className="w-full max-w-lg shadow-lg border-t-4 border-t-blue-600">
                    <CardHeader>
                        <CardTitle className="text-2xl text-center">Edit Vehicle #{vehicleId}</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <form onSubmit={handleSubmit} className="space-y-4">

                            {/* Image Preview & URL */}
                            <div className="space-y-2">
                                {displayImage && (
                                    <div className="h-48 w-full mb-2 rounded bg-gray-200 overflow-hidden">
                                        <img src={displayImage} alt="Car" className="w-full h-full object-cover" />
                                    </div>
                                )}
                                <Label htmlFor="imageUrl">Primary Image URL</Label>
                                <Input
                                    id="imageUrl"
                                    name="imageUrl"
                                    value={displayImage}
                                    onChange={(e) => setFormData({ ...formData, images: [e.target.value] })}
                                />
                            </div>

                            <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-2">
                                    <Label htmlFor="make">Make (Brand)</Label>
                                    <Input id="make" name="make" value={formData.make} onChange={handleChange} required />
                                </div>
                                <div className="space-y-2">
                                    <Label htmlFor="model">Model</Label>
                                    <Input id="model" name="model" value={formData.model} onChange={handleChange} required />
                                </div>
                            </div>

                            <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-2">
                                    <Label htmlFor="year">Year</Label>
                                    <Input id="year" name="year" type="number" value={formData.year} onChange={handleChange} required />
                                </div>
                                <div className="space-y-2">
                                    <Label htmlFor="dailyRate">Daily Price (Rs)</Label>
                                    <Input id="dailyRate" name="dailyRate" type="number" value={formData.dailyRate} onChange={handleChange} required />
                                </div>
                            </div>

                            <div className="flex items-center space-x-2 pt-4 border-t mt-4">
                                <Checkbox
                                    id="isAvailable"
                                    checked={formData.isAvailable}
                                    onCheckedChange={(checked: boolean) => setFormData({ ...formData, isAvailable: checked })}
                                />
                                <Label htmlFor="isAvailable" className="font-medium cursor-pointer">
                                    Vehicle is available for rent
                                </Label>
                            </div>

                            <Button type="submit" className="w-full bg-blue-600 hover:bg-blue-700 mt-6" disabled={loading}>
                                {loading ? "Saving Changes..." : "Save Changes"}
                            </Button>
                        </form>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}
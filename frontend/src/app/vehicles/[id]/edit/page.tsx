"use client";

import React, { useState, useEffect, use } from "react";
import { useRouter } from "next/navigation";
import { Navbar } from "@/components/Navbar";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Checkbox } from "@/components/ui/checkbox";

export default function EditVehiclePage({ params }: { params: Promise<{ id: string }> }) {
    const router = useRouter();
    const { id: vehicleId } = use(params);

    const [loading, setLoading] = useState(false);
    const [fetching, setFetching] = useState(true);

    const [formData, setFormData] = useState({
        make: "",
        model: "",
        vehicleType: "CAR",
        year: 2024,
        licensePlate: "",
        seatingCapacity: 4,
        dailyRate: "",
        isAvailable: true,
        imageUrl: "",
    });

    // Fetch Data
    useEffect(() => {
        async function fetchVehicle() {
            try {
                const res = await fetch(`http://localhost:8085/api/vehicles/${vehicleId}`);
                if (res.ok) {
                    const data = await res.json();
                    setFormData({
                        make: data.make,
                        model: data.model,
                        vehicleType: data.vehicleType || "CAR",
                        year: data.year,
                        licensePlate: data.licensePlate || "",
                        seatingCapacity: data.seatingCapacity,
                        dailyRate: data.dailyRate,
                        isAvailable: data.isAvailable,
                        imageUrl: (data.images && data.images[0]) ? data.images[0] : (data.imageUrl || ""),
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

    const handleChange = (e: any) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    const handleSubmit = async (e: any) => {
        e.preventDefault();
        setLoading(true);

        try {
            const payload = {
                vehicleType: formData.vehicleType,
                make: formData.make,
                model: formData.model,
                year: Number(formData.year),
                licensePlate: formData.licensePlate,
                seatingCapacity: Number(formData.seatingCapacity),
                dailyRate: Number(formData.dailyRate),
                images: formData.imageUrl ? [formData.imageUrl] : [],
            };

            const response = await fetch(`http://localhost:8085/api/vehicles/${vehicleId}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload),
            });

            if (response.ok) {
                alert("Vehicle Updated Successfully!");
                router.push("/admin");
            } else {
                const errText = await response.text();
                alert("Failed to update vehicle: " + errText);
            }
        } catch (error) {
            console.error("Error:", error);
            alert("Something went wrong!");
        } finally {
            setLoading(false);
        }
    };

    if (fetching) return <div className="text-center py-20 mt-10 text-xl">Loading...</div>;

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

                            {/* Image URL Section */}
                            <div className="space-y-2">
                                <Label>Vehicle Image URL</Label>
                                {formData.imageUrl && (
                                    <div className="mb-2">
                                        <p className="text-xs text-gray-500 mb-1">Current Image:</p>
                                        <img src={formData.imageUrl} alt="Current" className="h-32 w-full object-cover rounded bg-gray-100" />
                                    </div>
                                )}
                                <Input
                                    type="url"
                                    name="imageUrl"
                                    placeholder="https://example.com/image.jpg"
                                    value={formData.imageUrl}
                                    onChange={handleChange}
                                />
                                <p className="text-xs text-gray-500">Enter a URL for the vehicle image.</p>
                            </div>

                            <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-2">
                                    <Label htmlFor="make">Make</Label>
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
                                    onCheckedChange={(checked) => setFormData({ ...formData, isAvailable: checked as boolean })}
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
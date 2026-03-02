"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import { Navbar } from "@/components/Navbar";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
// NEW: Imported Select components for the Vehicle Type dropdown
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";

export default function AddVehiclePage() {
    const router = useRouter();
    const [loading, setLoading] = useState(false);

    const [formData, setFormData] = useState({
        make: "",
        model: "",
        vehicleType: "CAR", // Default value
        year: 2024,
        licensePlate: "",
        seatingCapacity: 4,
        dailyRate: "",
        features: []
    });

    // State to hold the actual file object
    const [imageFile, setImageFile] = useState<File | null>(null);

    const handleChange = (e: any) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files[0]) {
            setImageFile(e.target.files[0]);
        }
    };

    const handleSubmit = async (e: any) => {
        e.preventDefault();
        setLoading(true);

        try {
            let uploadedImageUrl = "";

            // STEP 1: Upload the image if the user selected one
            if (imageFile) {
                const imageFormData = new FormData();
                imageFormData.append("file", imageFile);

                const uploadRes = await fetch("http://localhost:8085/api/upload", {
                    method: "POST",
                    body: imageFormData,
                    // Do NOT set Content-Type manually when sending FormData
                });

                if (!uploadRes.ok) {
                    throw new Error("Failed to upload image");
                }

                uploadedImageUrl = await uploadRes.text(); // Get the URL back
            }

            // STEP 2: Save the vehicle with the new image URL
            const payload = {
                ...formData,
                year: Number(formData.year),
                seatingCapacity: Number(formData.seatingCapacity),
                dailyRate: Number(formData.dailyRate),
                images: uploadedImageUrl ? [uploadedImageUrl] : [], // Attach the new URL
            };

            const response = await fetch("http://localhost:8085/api/vehicles", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "X-User-Id": "admin" // Mock user ID
                },
                body: JSON.stringify(payload),
            });

            if (response.ok) {
                alert("Vehicle Added Successfully!");
                router.push("/vehicles");
            } else {
                const errText = await response.text();
                alert("Failed to add vehicle: " + errText);
            }
        } catch (error) {
            console.error("Error:", error);
            alert("Something went wrong with the upload!");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-gray-50">
            <Navbar />
            <div className="container mx-auto px-4 py-12 flex justify-center">
                <Card className="w-full max-w-lg shadow-lg border-t-4 border-t-blue-600">
                    <CardHeader>
                        <CardTitle className="text-2xl text-center">List Your Vehicle</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <form onSubmit={handleSubmit} className="space-y-4">

                            {/* FILE UPLOAD INPUT */}
                            <div className="space-y-2">
                                <Label htmlFor="imageFile">Vehicle Image</Label>
                                <Input
                                    id="imageFile"
                                    name="imageFile"
                                    type="file"
                                    accept="image/*"
                                    onChange={handleFileChange}
                                />
                            </div>

                            {/* NEW: VEHICLE TYPE DROPDOWN */}
                            <div className="space-y-2">
                                <Label>Vehicle Type</Label>
                                <Select
                                    value={formData.vehicleType}
                                    onValueChange={(val) => setFormData({ ...formData, vehicleType: val })}
                                >
                                    <SelectTrigger className="bg-white">
                                        <SelectValue placeholder="Select Vehicle Type" />
                                    </SelectTrigger>
                                    <SelectContent className="bg-white z-50 shadow-md border">
                                        <SelectItem value="CAR">Car</SelectItem>
                                        <SelectItem value="SUV">SUV</SelectItem>
                                        <SelectItem value="VAN">Van</SelectItem>
                                        <SelectItem value="BUS">Bus</SelectItem>
                                        <SelectItem value="TUK_TUK">Tuk-Tuk</SelectItem>
                                        <SelectItem value="MOTORBIKE">Motorbike</SelectItem>
                                    </SelectContent>
                                </Select>
                            </div>

                            <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-2">
                                    <Label htmlFor="make">Make</Label>
                                    <Input id="make" name="make" placeholder="Toyota" onChange={handleChange} required />
                                </div>
                                <div className="space-y-2">
                                    <Label htmlFor="model">Model</Label>
                                    <Input id="model" name="model" placeholder="Prius" onChange={handleChange} required />
                                </div>
                            </div>

                            <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-2">
                                    <Label htmlFor="year">Year</Label>
                                    <Input id="year" name="year" type="number" defaultValue={2024} onChange={handleChange} required />
                                </div>
                                <div className="space-y-2">
                                    <Label htmlFor="seatingCapacity">Seats</Label>
                                    <Input id="seatingCapacity" name="seatingCapacity" type="number" defaultValue={4} onChange={handleChange} required />
                                </div>
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="licensePlate">License Plate</Label>
                                <Input id="licensePlate" name="licensePlate" placeholder="ABC-1234" onChange={handleChange} required />
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="dailyRate">Daily Price (Rs)</Label>
                                <Input id="dailyRate" name="dailyRate" type="number" placeholder="50.00" step="0.01" onChange={handleChange} required />
                            </div>

                            <Button type="submit" className="w-full bg-blue-600 hover:bg-blue-700 mt-6" disabled={loading}>
                                {loading ? "Uploading & Saving..." : "List Vehicle"}
                            </Button>
                        </form>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}
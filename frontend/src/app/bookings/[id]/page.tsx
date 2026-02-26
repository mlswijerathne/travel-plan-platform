"use client";

import React, { useState, useEffect, use } from "react";
import { useRouter } from "next/navigation";
import { Navbar } from "@/components/Navbar";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { CalendarIcon } from "lucide-react";

export default function BookingPage({ params }: { params: Promise<{ id: string }> }) {
    const router = useRouter();
    const { id: vehicleId } = use(params);

    const [vehicle, setVehicle] = useState<any>(null);
    const [loading, setLoading] = useState(false);

    // UPDATED: Added customerEmail to state
    const [formData, setFormData] = useState({
        customerName: "",
        customerEmail: "",
        startDate: "",
        endDate: "",
    });

    const [totalPrice, setTotalPrice] = useState(0);

    // 1. Fetch Vehicle Details
    useEffect(() => {
        fetch(`http://localhost:8085/api/vehicles/${vehicleId}`)
            .then(res => res.json())
            .then(data => setVehicle(data))
            .catch(err => console.error("Error loading vehicle", err));
    }, [vehicleId]);

    // 2. Calculate Price
    useEffect(() => {
        if (vehicle && formData.startDate && formData.endDate) {
            const start = new Date(formData.startDate);
            const end = new Date(formData.endDate);
            const diffTime = Math.abs(end.getTime() - start.getTime());
            const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

            if (diffDays > 0) {
                setTotalPrice(diffDays * vehicle.dailyRate);
            } else {
                setTotalPrice(0);
            }
        }
    }, [formData.startDate, formData.endDate, vehicle]);

    const handleChange = (e: any) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    // 3. Submit Booking
    const handleSubmit = async (e: any) => {
        e.preventDefault();
        setLoading(true);

        const bookingData = {
            vehicleId: parseInt(vehicleId),
            customerName: formData.customerName,
            customerEmail: formData.customerEmail, // UPDATED: Sending Email now
            startDate: formData.startDate,
            endDate: formData.endDate,
            totalPrice: totalPrice,
            status: "CONFIRMED"
        };

        try {
            const res = await fetch("http://localhost:8085/api/bookings", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(bookingData)
            });

            if (res.ok) {
                alert("Booking Successful! The car is yours.");
                router.push("/vehicles");
            } else {
                const errorMsg = await res.text();
                alert("Booking Failed: " + errorMsg);
            }
        } catch (error) {
            console.error("Error booking:", error);
            alert("Something went wrong.");
        } finally {
            setLoading(false);
        }
    };

    if (!vehicle) return <div className="text-center py-20">Loading Booking Details...</div>;

    // FIX: Helper to extract the image from the new array structure or fallback to legacy imageUrl
    const displayImage = vehicle?.images?.[0] || vehicle?.imageUrl;

    return (
        <div className="min-h-screen bg-gray-50 pb-20">
            <Navbar />
            <div className="container mx-auto px-4 py-12 flex justify-center">
                <Card className="w-full max-w-2xl shadow-xl overflow-hidden grid grid-cols-1 md:grid-cols-2">

                    {/* LEFT SIDE: Car Details */}
                    <div className="bg-gray-900 text-white p-6 flex flex-col justify-between">
                        <div>
                            <h2 className="text-2xl font-bold mb-2">{vehicle.make} {vehicle.model}</h2>
                            <p className="text-gray-400 mb-6">{vehicle.year} • {vehicle.seatingCapacity} Seats</p>

                            {/* FIX: Now looking at displayImage which safely pulls from the array */}
                            <div className="h-48 bg-gray-800 rounded-lg overflow-hidden mb-4 flex items-center justify-center">
                                {displayImage ? (
                                    <img src={displayImage} alt={`${vehicle.make} ${vehicle.model}`} className="w-full h-full object-cover" />
                                ) : (
                                    <span className="text-gray-500 text-sm">No Image Available</span>
                                )}
                            </div>
                        </div>
                        <div className="border-t border-gray-700 pt-4">
                            <div className="flex justify-between items-center text-lg">
                                <span>Daily Rate:</span>
                                <span className="font-bold text-green-400">Rs {vehicle.dailyRate}</span>
                            </div>
                            <div className="flex justify-between items-center text-xl font-bold mt-2">
                                <span>Total:</span>
                                <span>Rs {totalPrice}</span>
                            </div>
                        </div>
                    </div>

                    {/* RIGHT SIDE: Booking Form */}
                    <div className="p-6 bg-white">
                        <CardHeader className="px-0 pt-0">
                            <CardTitle>Confirm Booking</CardTitle>
                        </CardHeader>
                        <CardContent className="px-0">
                            <form onSubmit={handleSubmit} className="space-y-4">

                                <div className="space-y-2">
                                    <Label>Full Name</Label>
                                    <Input
                                        name="customerName"
                                        placeholder="John Doe"
                                        required
                                        onChange={handleChange}
                                    />
                                </div>

                                {/* NEW EMAIL INPUT */}
                                <div className="space-y-2">
                                    <Label>Email Address</Label>
                                    <Input
                                        type="email"
                                        name="customerEmail"
                                        placeholder="john@example.com"
                                        required
                                        onChange={handleChange}
                                    />
                                </div>

                                <div className="grid grid-cols-2 gap-4">
                                    <div className="space-y-2">
                                        <Label>Pick-up Date</Label>
                                        <Input
                                            type="date"
                                            name="startDate"
                                            required
                                            onChange={handleChange}
                                        />
                                    </div>
                                    <div className="space-y-2">
                                        <Label>Return Date</Label>
                                        <Input
                                            type="date"
                                            name="endDate"
                                            required
                                            onChange={handleChange}
                                        />
                                    </div>
                                </div>

                                <div className="bg-blue-50 text-blue-800 p-3 rounded-md text-sm flex items-start gap-2">
                                    <CalendarIcon size={16} className="mt-1" />
                                    <p>Your booking will be instantly confirmed. Payment is collected upon pick-up.</p>
                                </div>

                                <Button type="submit" className="w-full bg-blue-600 hover:bg-blue-700 h-12 text-lg" disabled={loading}>
                                    {loading ? "Processing..." : "Confirm Booking"}
                                </Button>

                            </form>
                        </CardContent>
                    </div>

                </Card>
            </div>
        </div>
    );
}
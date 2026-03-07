import React from "react";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { ArrowLeft, User, Fuel } from "lucide-react";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8060';

// Fetch details for a SINGLE vehicle via API Gateway
async function getVehicleById(id: string) {
    const res = await fetch(`${API_BASE}/api/vehicles/${id}`, { cache: 'no-store' });
    if (!res.ok) {
        return null; // Handle error gracefully
    }
    return res.json();
}

export default async function VehicleDetailsPage({ params }: { params: Promise<{ id: string }> }) {
    // Await params to get the ID (Next.js 15+ requirement)
    const { id } = await params;
    const vehicle = await getVehicleById(id);

    if (!vehicle) {
        return (
            <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center">
                <h1 className="text-2xl font-bold mb-4">Vehicle Not Found</h1>
                <Link href="/vehicles"><Button>Go Back</Button></Link>
            </div>
        );
    }

    return (
        <div>
                {/* Back Button */}
                <Link href="/vehicles" className="flex items-center text-gray-500 hover:text-black mb-6">
                    <ArrowLeft size={20} className="mr-2" /> Back to List
                </Link>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-12">

                    {/* Left Side: Vehicle Image */}
                    <div className="bg-gray-200 rounded-xl h-[400px] flex items-center justify-center text-gray-400 overflow-hidden">
                        {vehicle.images?.[0] || vehicle.imageUrl ? (
                            <img src={vehicle.images?.[0] || vehicle.imageUrl} alt={`${vehicle.make} ${vehicle.model}`} className="w-full h-full object-cover" />
                        ) : (
                            <span className="text-xl">No Image Available</span>
                        )}
                    </div>

                    {/* Right Side: Details */}
                    <div>
                        <div className="flex items-center justify-between mb-4">
                            <Badge className="text-sm px-3 py-1" variant={vehicle.isAvailable ? "default" : "destructive"}>
                                {vehicle.isAvailable ? "Available Now" : "Currently Booked"}
                            </Badge>
                            <span className="text-gray-500 font-medium">ID: #{vehicle.id}</span>
                        </div>

                        <h1 className="text-4xl font-extrabold mb-2">{vehicle.make} {vehicle.model}</h1>
                        <p className="text-xl text-gray-600 mb-6">{vehicle.year} • {vehicle.vehicleType}</p>

                        <div className="grid grid-cols-2 gap-4 mb-8">
                            <Card>
                                <CardContent className="flex items-center p-4 gap-4">
                                    <User className="text-primary" />
                                    <div>
                                        <p className="text-xs text-gray-500">Seats</p>
                                        <p className="font-bold">{vehicle.seatingCapacity} Persons</p>
                                    </div>
                                </CardContent>
                            </Card>
                            <Card>
                                <CardContent className="flex items-center p-4 gap-4">
                                    <Fuel className="text-primary" />
                                    <div>
                                        <p className="text-xs text-gray-500">Fuel Type</p>
                                        <p className="font-bold">Petrol</p>
                                    </div>
                                </CardContent>
                            </Card>
                        </div>

                        <div className="border-t pt-6">
                            <div className="flex items-end justify-between mb-6">
                                <div>
                                    <p className="text-gray-500">Daily Rate</p>
                                    <p className="text-4xl font-bold text-primary">${vehicle.dailyRate}</p>
                                </div>
                            </div>

                            {/* UPDATED BUTTON: Links to the Booking Form */}
                            <Link href={`/bookings?vehicleId=${vehicle.id}&make=${vehicle.make}&model=${vehicle.model}&price=${vehicle.dailyRate}`}>
                                <Button size="lg" className="w-full text-lg h-12">
                                    Proceed to Booking
                                </Button>
                            </Link>
                        </div>
                    </div>
                </div>
        </div>
    );
}
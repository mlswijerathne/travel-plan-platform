import React from "react";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { ArrowLeft, User, Fuel, Car } from "lucide-react";

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
            <div className="min-h-screen bg-background flex flex-col items-center justify-center">
                <h1 className="font-display text-2xl font-extrabold tracking-tight mb-4">Vehicle Not Found</h1>
                <Link href="/vehicles"><Button>Go Back</Button></Link>
            </div>
        );
    }

    return (
        <div>
                {/* Back Button */}
                <Link href="/vehicles" className="flex items-center text-muted-foreground hover:text-foreground mb-6">
                    <ArrowLeft size={20} className="mr-2" /> Back to List
                </Link>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-12">

                    {/* Left Side: Vehicle Image */}
                    <div className="bg-surface-low rounded-2xl h-[400px] flex items-center justify-center overflow-hidden">
                        {vehicle.images?.[0] || vehicle.imageUrl ? (
                            <img src={vehicle.images?.[0] || vehicle.imageUrl} alt={`${vehicle.make} ${vehicle.model}`} className="w-full h-full object-cover" />
                        ) : (
                            <Car className="h-20 w-20 text-muted-foreground/30" />
                        )}
                    </div>

                    {/* Right Side: Details */}
                    <div className="bg-card rounded-xl shadow-editorial p-8">
                        <div className="flex items-center justify-between mb-4">
                            <Badge className="text-sm px-3 py-1" variant={vehicle.isAvailable ? "default" : "destructive"}>
                                {vehicle.isAvailable ? "Available Now" : "Currently Booked"}
                            </Badge>
                            <span className="text-muted-foreground font-medium">ID: #{vehicle.id}</span>
                        </div>

                        <h1 className="font-display text-4xl font-extrabold tracking-tight text-foreground mb-2">{vehicle.make} {vehicle.model}</h1>
                        <p className="text-xl text-muted-foreground mb-6">{vehicle.year} • {vehicle.vehicleType}</p>

                        <div className="grid grid-cols-2 gap-4 mb-8">
                            <Card className="bg-card rounded-xl shadow-editorial border border-border/30">
                                <CardContent className="flex items-center p-4 gap-4">
                                    <User className="text-primary" />
                                    <div>
                                        <p className="text-xs text-muted-foreground">Seats</p>
                                        <p className="font-bold">{vehicle.seatingCapacity} Persons</p>
                                    </div>
                                </CardContent>
                            </Card>
                            <Card className="bg-card rounded-xl shadow-editorial border border-border/30">
                                <CardContent className="flex items-center p-4 gap-4">
                                    <Fuel className="text-primary" />
                                    <div>
                                        <p className="text-xs text-muted-foreground">Fuel Type</p>
                                        <p className="font-bold">Petrol</p>
                                    </div>
                                </CardContent>
                            </Card>
                        </div>

                        <div className="border-t border-border/30 pt-6">
                            <div className="flex items-end justify-between mb-6">
                                <div>
                                    <p className="text-muted-foreground">Daily Rate</p>
                                    <p className="font-display text-3xl font-extrabold text-primary">${vehicle.dailyRate}</p>
                                </div>
                            </div>

                            {/* UPDATED BUTTON: Links to the Booking Form */}
                            <Link href={`/bookings?vehicleId=${vehicle.id}&make=${vehicle.make}&model=${vehicle.model}&price=${vehicle.dailyRate}`}>
                                <Button size="lg" className="bg-secondary hover:bg-secondary/90 text-white w-full py-6 rounded-xl font-display font-bold text-lg">
                                    Proceed to Booking
                                </Button>
                            </Link>
                        </div>
                    </div>
                </div>
        </div>
    );
}
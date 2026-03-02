import React from "react";
import Link from "next/link";
import { Card, CardContent, CardFooter } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Users, Fuel } from "lucide-react";

interface VehicleProps {
    id: number;
    imageUrl?: string;
    make: string;
    model: string;
    type: string;
    year: number;
    seats: number;
    price: number;
    available: boolean;
}

export function VehicleCard({
    id,
    imageUrl,
    make,
    model,
    type,
    year,
    seats,
    price,
    available
}: VehicleProps) {
    return (
        <Card className="overflow-hidden hover:shadow-xl transition-all duration-300 border-none shadow-md bg-white flex flex-col h-full">

            {/* IMAGE SECTION */}
            <div className="relative h-48 w-full bg-gray-100">
                {imageUrl ? (
                    <img
                        src={imageUrl}
                        alt={`${make} ${model}`}
                        className="w-full h-full object-cover"
                    />
                ) : (
                    <div className="w-full h-full flex items-center justify-center text-gray-400">
                        No Image Available
                    </div>
                )}

                {/* Availability Badge */}
                <div className="absolute top-3 left-3">
                    <Badge variant={available ? "default" : "destructive"} className={available ? "bg-white text-black hover:bg-white" : ""}>
                        {available ? "Available" : "Booked"}
                    </Badge>
                </div>
            </div>

            {/* CONTENT SECTION */}
            <CardContent className="p-5 grow">
                <div className="flex justify-between items-start mb-2">
                    <div>
                        <h3 className="text-xl font-bold text-gray-900">{make} {model}</h3>
                        <p className="text-xs text-gray-500 uppercase tracking-wider">{type}</p>
                    </div>
                    <span className="text-sm font-medium text-gray-400">{year}</span>
                </div>

                <div className="grid grid-cols-2 gap-y-2 mt-4 text-sm text-gray-600">
                    <div className="flex items-center gap-2">
                        <Users size={16} />
                        <span>{seats} Seats</span>
                    </div>
                    <div className="flex items-center gap-2">
                        <Fuel size={16} />
                        <span>Petrol</span>
                    </div>
                </div>
            </CardContent>

            {/* FOOTER / PRICE & ACTION */}
            <CardFooter className="p-5 pt-0 flex flex-col gap-4">
                <div className="w-full flex items-end gap-1">
                    <span className="text-2xl font-bold text-gray-900">Rs {price}</span>
                    <span className="text-gray-500 mb-1">/ day</span>
                </div>

                {/* BOOK BUTTON LOGIC - UPDATED LINK BELOW */}
                {available ? (
                    <Link href={`/bookings/${id}`} className="w-full">
                        <Button className="w-full bg-gray-900 hover:bg-gray-800 text-white font-medium h-10">
                            Book Now
                        </Button>
                    </Link>
                ) : (
                    <Button disabled className="w-full bg-gray-100 text-gray-400 hover:bg-gray-100 cursor-not-allowed">
                        Unavailable
                    </Button>
                )}
            </CardFooter>
        </Card>
    );
}
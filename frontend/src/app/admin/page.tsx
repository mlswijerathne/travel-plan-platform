"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import { Navbar } from "@/components/Navbar";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Trash2, CheckCircle, Pencil } from "lucide-react";

export default function AdminDashboard() {
    const [bookings, setBookings] = useState<any[]>([]);
    const [vehicles, setVehicles] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);

    // Fetch both Vehicles and Bookings from the backend
    const fetchData = async () => {
        try {
            const [bookingsRes, vehiclesRes] = await Promise.all([
                fetch("http://localhost:8085/api/bookings"),
                fetch("http://localhost:8085/api/vehicles?size=100") // Fetch up to 100 vehicles
            ]);

            if (bookingsRes.ok) {
                setBookings(await bookingsRes.json());
            }
            if (vehiclesRes.ok) {
                const vehiclePage = await vehiclesRes.json();
                // Extract the content array from Spring's Page object
                setVehicles(vehiclePage.content ?? vehiclePage);
            }
        } catch (error) {
            console.error("Failed to load admin data", error);
        } finally {
            setLoading(false);
        }
    };

    // Load data on page mount
    useEffect(() => {
        fetchData();
    }, []);

    // COMPLETE BOOKING LOGIC
    const handleCompleteBooking = async (bookingId: number) => {
        if (!bookingId) {
            alert("Error: No Booking ID found!");
            return;
        }

        if (!confirm("Has the customer returned the car? This will mark the car as available again.")) return;

        try {
            const res = await fetch(`http://localhost:8085/api/bookings/${bookingId}/complete`, {
                method: "PUT"
            });

            if (res.ok) {
                alert("Car returned successfully! It is now available for rent.");
                fetchData(); // Refresh the tables instantly
            } else if (res.status === 404) {
                alert("Error 404: The server couldn't find this booking. It may have been deleted.");
            } else {
                alert("Failed to complete booking.");
            }
        } catch (error) {
            console.error("Error completing booking:", error);
            alert("Network error: Could not reach the server.");
        }
    };

    // DELETE VEHICLE LOGIC
    const handleDeleteVehicle = async (vehicleId: number) => {
        if (!confirm("Are you sure you want to delete this vehicle?")) return;

        try {
            const res = await fetch(`http://localhost:8085/api/vehicles/${vehicleId}`, {
                method: "DELETE",
                headers: { "X-User-Id": "admin" } // Pass admin context
            });

            if (res.ok) {
                fetchData(); // Refresh the table instantly
            } else {
                alert("Failed to delete vehicle.");
            }
        } catch (error) {
            console.error("Failed to delete vehicle", error);
        }
    };

    if (loading) return <div className="text-center py-20">Loading Admin Dashboard...</div>;

    return (
        <div className="min-h-screen bg-gray-50 pb-20">
            <Navbar />
            <div className="container mx-auto px-4 py-12">
                <h1 className="text-3xl font-bold mb-8">Admin Control Center</h1>
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">

                    {/* Vehicles Table */}
                    <Card>
                        <CardHeader className="flex flex-row items-center justify-between pb-2">
                            <CardTitle className="text-xl">Fleet Management</CardTitle>
                            <Badge className="bg-black text-white">{vehicles.length} Cars</Badge>
                        </CardHeader>
                        <CardContent>
                            <table className="w-full text-sm">
                                <thead>
                                    <tr className="border-b text-gray-500">
                                        <th className="text-left pb-3 font-medium">ID</th>
                                        <th className="text-left pb-3 font-medium">Vehicle</th>
                                        <th className="text-center pb-3 font-medium">Status</th>
                                        <th className="text-right pb-3 font-medium">Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {vehicles.map((vehicle) => (
                                        <tr key={vehicle.id} className="border-b last:border-0">
                                            <td className="py-4 font-medium">#{vehicle.id}</td>
                                            <td className="py-4 font-bold">{vehicle.make} {vehicle.model}</td>
                                            <td className="py-4 text-center">
                                                <Badge variant={vehicle.isAvailable ? "outline" : "secondary"}>
                                                    {vehicle.isAvailable ? "Available" : "Booked"}
                                                </Badge>
                                            </td>
                                            <td className="py-4 text-right flex justify-end gap-2">
                                                {/* Edit Button */}
                                                <Link href={`/vehicles/${vehicle.id}/edit`}>
                                                    <Button variant="outline" size="icon" className="text-blue-600 border-blue-600 hover:bg-blue-50">
                                                        <Pencil size={16} />
                                                    </Button>
                                                </Link>
                                                {/* Delete Button */}
                                                <Button variant="outline" size="icon" className="text-red-600 border-red-600 hover:bg-red-50" onClick={() => handleDeleteVehicle(vehicle.id)}>
                                                    <Trash2 size={16} />
                                                </Button>
                                            </td>
                                        </tr>
                                    ))}
                                    {vehicles.length === 0 && (
                                        <tr>
                                            <td colSpan={4} className="text-center py-4 text-gray-500">No vehicles found.</td>
                                        </tr>
                                    )}
                                </tbody>
                            </table>
                        </CardContent>
                    </Card>

                    {/* Bookings Table */}
                    <Card>
                        <CardHeader className="flex flex-row items-center justify-between pb-2">
                            <CardTitle className="text-xl">Recent Bookings</CardTitle>
                            <Badge className="bg-blue-600 text-white">{bookings.length} Orders</Badge>
                        </CardHeader>
                        <CardContent>
                            <table className="w-full text-sm">
                                <thead>
                                    <tr className="border-b text-gray-500">
                                        <th className="text-left pb-3 font-medium">ID</th>
                                        <th className="text-left pb-3 font-medium">Customer</th>
                                        <th className="text-center pb-3 font-medium">Status</th>
                                        <th className="text-right pb-3 font-medium">Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {bookings.map((booking) => (
                                        <tr key={booking.id} className="border-b last:border-0">
                                            <td className="py-4 font-medium">#{booking.id}</td>
                                            <td className="py-4">
                                                <div className="font-bold">{booking.customerName}</div>
                                                <div className="text-xs text-gray-500">Car #{booking.vehicleId}</div>
                                            </td>
                                            <td className="py-4 text-center">
                                                <Badge className={booking.status === "COMPLETED" ? "bg-gray-200 text-gray-600" : "bg-blue-50 text-blue-700"}>
                                                    {booking.status}
                                                </Badge>
                                            </td>
                                            <td className="p-3 text-right">
                                                {booking.status !== "COMPLETED" && (
                                                    <Button
                                                        variant="outline"
                                                        size="sm"
                                                        className="text-green-600 border-green-600 hover:bg-green-50"
                                                        onClick={() => handleCompleteBooking(booking.id)}
                                                    >
                                                        <CheckCircle size={16} className="mr-2" /> Complete
                                                    </Button>
                                                )}
                                            </td>
                                        </tr>
                                    ))}
                                    {bookings.length === 0 && (
                                        <tr>
                                            <td colSpan={4} className="text-center py-4 text-gray-500">No bookings yet.</td>
                                        </tr>
                                    )}
                                </tbody>
                            </table>
                        </CardContent>
                    </Card>

                </div>
            </div>
        </div>
    );
}
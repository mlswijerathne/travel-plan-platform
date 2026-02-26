"use client";

import React, { useState, Suspense } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import { Navbar } from '@/components/Navbar';
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

function BookingForm() {
  const searchParams = useSearchParams();
  const router = useRouter();
  
  // Get data from the URL (passed from the previous page)
  const vehicleId = searchParams.get('vehicleId');
  const make = searchParams.get('make');
  const model = searchParams.get('model');
  const price = searchParams.get('price');

  const [formData, setFormData] = useState({
    customerName: "",
    customerEmail: "",
    startDate: "",
    endDate: ""
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // 1. Prepare the data for Java
    const bookingData = {
      vehicleId: Number(vehicleId),
      customerName: formData.customerName,
      customerEmail: formData.customerEmail,
      startDate: formData.startDate,
      endDate: formData.endDate,
      totalPrice: Number(price) * 2, // Simple logic: assumes 2 days for now (we can improve later)
      status: "CONFIRMED"
    };

    // 2. Send to Backend
    try {
      const res = await fetch("http://localhost:8085/api/bookings", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(bookingData)
      });

      if (res.ok) {
        alert("Booking Successful! The car is yours.");
        router.push("/vehicles"); // Go back to home
      } else {
        alert("Booking Failed. Please try again.");
      }
    } catch (error) {
      console.error("Error booking:", error);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="container mx-auto px-4 py-12 flex justify-center">
        <Card className="w-full max-w-lg shadow-lg">
          <CardHeader className="bg-black text-white rounded-t-lg">
            <CardTitle>Complete Your Booking</CardTitle>
            <p className="text-gray-300 text-sm">You are booking: {make} {model}</p>
          </CardHeader>
          <CardContent className="pt-6">
            <form onSubmit={handleSubmit} className="space-y-4">
              
              <div className="space-y-2">
                <Label>Full Name</Label>
                <Input 
                  required 
                  placeholder="John Doe" 
                  onChange={(e) => setFormData({...formData, customerName: e.target.value})}
                />
              </div>

              <div className="space-y-2">
                <Label>Email Address</Label>
                <Input 
                  required 
                  type="email" 
                  placeholder="john@example.com" 
                  onChange={(e) => setFormData({...formData, customerEmail: e.target.value})}
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label>Start Date</Label>
                  <Input 
                    required 
                    type="date" 
                    onChange={(e) => setFormData({...formData, startDate: e.target.value})}
                  />
                </div>
                <div className="space-y-2">
                  <Label>End Date</Label>
                  <Input 
                    required 
                    type="date" 
                    onChange={(e) => setFormData({...formData, endDate: e.target.value})}
                  />
                </div>
              </div>

              <div className="pt-4 border-t mt-4">
                <div className="flex justify-between mb-4 text-sm font-medium">
                  <span>Daily Rate:</span>
                  <span>${price}</span>
                </div>
                <Button type="submit" className="w-full bg-green-600 hover:bg-green-700 text-lg py-6">
                  Confirm & Pay
                </Button>
              </div>

            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

// Wrap in Suspense (Required for Next.js when using searchParams)
export default function BookingPage() {
  return (
    <Suspense fallback={<div>Loading...</div>}>
      <BookingForm />
    </Suspense>
  );
}
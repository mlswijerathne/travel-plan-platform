"use client";

import React, { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import Link from "next/link";
import { TripPackage } from "@/types/trip-package";
import { MapPin, Clock, Users, ArrowLeft, Edit, Trash2, Loader2 } from "lucide-react";
import { getPackage, deletePackage } from "@/lib/api/packages";

export default function PackageDetailsPage() {
    const { id } = useParams();
    const router = useRouter();

    const [pkg, setPkg] = useState<TripPackage | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const [isDeleting, setIsDeleting] = useState(false);

    useEffect(() => {
        if (!id) return;

        getPackage(Number(id))
            .then((json) => {
                setPkg(json.data);
                setLoading(false);
            })
            .catch((err) => {
                console.error(err);
                setError("Could not load the travel package.");
                setLoading(false);
            });
    }, [id]);

    // The Delete Function
    const handleDelete = async () => {
        // 1. Ask for confirmation before doing anything dangerous!
        const confirmDelete = window.confirm("Are you sure you want to delete this travel package? This action cannot be undone.");
        if (!confirmDelete) return;

        setIsDeleting(true);

        try {
            await deletePackage(Number(id));
            router.push("/packages");
        } catch (err) {
            console.error("Delete Error:", err);
            alert("An error occurred while trying to delete the package.");
            setIsDeleting(false);
        }
    };

    if (loading) return <div className="min-h-screen flex items-center justify-center bg-surface-low"><div className="animate-pulse text-2xl font-bold text-primary">Loading itinerary details...</div></div>;
    if (error || !pkg) return <div className="min-h-screen flex items-center justify-center bg-surface-low text-red-600 font-bold flex-col"><p>{error || "Package not found."}</p><button onClick={() => router.back()} className="mt-4 text-primary underline">Go Back</button></div>;

    const finalPrice = pkg.basePrice - (pkg.basePrice * (pkg.discountPercentage / 100));

    return (
        <div className="min-h-screen bg-surface-low pb-20">
            {/* Hero Header */}
            <div className="w-full h-[400px] bg-primary relative flex flex-col justify-end p-8 shadow-inner">
                {pkg.images && pkg.images.length > 0 && (
                    <img src={pkg.images[0]} alt={pkg.name} className="absolute inset-0 w-full h-full object-cover mix-blend-overlay opacity-60" />
                )}
                <button onClick={() => router.back()} className="absolute top-8 left-8 text-white flex items-center hover:text-white/70 transition-colors z-10">
                    <ArrowLeft className="w-5 h-5 mr-2" /> Back to Packages
                </button>
                <div className="relative z-10">
                    <h1 className="text-4xl md:text-5xl font-extrabold text-white mb-2 shadow-sm">{pkg.name}</h1>
                    <p className="text-white/70 text-lg flex items-center font-medium">
                        <MapPin className="w-5 h-5 mr-2" /> {pkg.destinations?.join(" → ")}
                    </p>
                </div>
            </div>

            <div className="container mx-auto px-4 mt-8 grid grid-cols-1 lg:grid-cols-3 gap-8">

                {/* Left Column: Details */}
                <div className="lg:col-span-2 space-y-8">
                    <div className="bg-white p-6 rounded-xl shadow-sm border border-border">
                        <h2 className="text-2xl font-bold text-foreground mb-4">Trip Overview</h2>
                        <p className="text-muted-foreground leading-relaxed mb-6">{pkg.description}</p>
                        <div className="flex flex-wrap gap-6 text-muted-foreground">
                            <div className="flex items-center"><Clock className="w-5 h-5 mr-2 text-primary" /><span className="font-semibold">{pkg.durationDays} Days</span></div>
                            {pkg.maxParticipants && <div className="flex items-center"><Users className="w-5 h-5 mr-2 text-primary" /><span className="font-semibold">Max {pkg.maxParticipants} People</span></div>}
                        </div>
                    </div>
                </div>

                {/* Right Column: Pricing & Booking */}
                <div className="lg:col-span-1">
                    <div className="bg-white p-6 rounded-xl shadow-lg border border-border sticky top-8">
                        <h3 className="text-xl font-bold text-foreground mb-6">Booking Summary</h3>
                        <div className="space-y-4 mb-6">
                            <div className="flex justify-between text-muted-foreground"><span>Base Price</span><span>Rs. {pkg.basePrice.toFixed(2)}</span></div>
                            <div className="pt-4 border-t border-border flex justify-between items-end">
                                <span className="text-lg font-bold text-foreground">Total Price</span>
                                <span className="text-3xl font-black text-primary">Rs. {finalPrice.toFixed(2)}</span>
                            </div>
                        </div>

                        {/* Book This Package → navigates to booking wizard */}
                        <Link
                            href={`/bookings/new?packageId=${id}&packageName=${encodeURIComponent(pkg.name)}&price=${finalPrice}&days=${pkg.durationDays}`}
                            className="w-full font-bold py-4 rounded-lg shadow-md transition-colors text-lg flex justify-center items-center bg-primary hover:bg-primary/90 text-white"
                        >
                            Book This Package
                        </Link>
                        <p className="text-center text-xs text-muted-foreground mt-4">
                            Instant confirmation via email.
                        </p>

                        {/* Admin Actions Section */}
                        <div className="mt-8 pt-6 border-t border-border">
                            <p className="text-xs text-muted-foreground uppercase font-bold tracking-wider mb-3">Admin Actions</p>

                            <div className="space-y-3">
                                {/* Edit Button */}
                                <button
                                    onClick={() => router.push(`/admin/packages/${id}/edit`)}
                                    className="w-full flex items-center justify-center bg-surface-low hover:bg-surface-high text-foreground font-semibold py-3 rounded-lg transition-colors border border-border"
                                >
                                    <Edit className="w-5 h-5 mr-2" /> Edit Package Details
                                </button>

                                {/* New Delete Button */}
                                <button
                                    onClick={handleDelete}
                                    disabled={isDeleting}
                                    className="w-full flex items-center justify-center bg-red-50 hover:bg-red-100 text-red-600 font-semibold py-3 rounded-lg transition-colors border border-red-200 disabled:opacity-70"
                                >
                                    {isDeleting ? (
                                        <><Loader2 className="w-5 h-5 mr-2 animate-spin" /> Deleting...</>
                                    ) : (
                                        <><Trash2 className="w-5 h-5 mr-2" /> Delete Package</>
                                    )}
                                </button>
                            </div>
                        </div>

                    </div>
                </div>
            </div>
        </div>
    );
}
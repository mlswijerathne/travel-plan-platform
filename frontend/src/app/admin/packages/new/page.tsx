"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import { ArrowLeft, Loader2, PlusCircle, CheckCircle, UploadCloud } from "lucide-react";
import { createClient } from "@supabase/supabase-js";

// Initialize Supabase client using your existing .env.local keys
const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL as string;
const supabaseKey = process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY as string;
const supabase = createClient(supabaseUrl, supabaseKey);

export default function CreatePackagePage() {
    const router = useRouter();

    const [formData, setFormData] = useState({
        name: "",
        description: "",
        durationDays: 1,
        basePrice: 0,
        discountPercentage: 0,
        maxParticipants: 10,
        destinations: "",
    });

    // State specifically for the image file
    const [imageFile, setImageFile] = useState<File | null>(null);
    const [imagePreview, setImagePreview] = useState<string | null>(null);

    const [status, setStatus] = useState<'idle' | 'loading' | 'success' | 'error'>('idle');
    const [errorMessage, setErrorMessage] = useState("");

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    // Handle when the admin selects a file from their PC
    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files.length > 0) {
            const file = e.target.files[0];
            setImageFile(file);
            setImagePreview(URL.createObjectURL(file)); // Show a quick preview on the screen
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setStatus('loading');
        setErrorMessage("");

        try {
            let finalImageUrl = "https://images.unsplash.com/photo-1588215017058-29759d57a9f7?w=800&q=80"; // Default fallback

            // 1. If the admin selected a file, upload it to Supabase first!
            if (imageFile) {
                // Create a unique file name so images don't overwrite each other
                const fileExt = imageFile.name.split('.').pop();
                const fileName = `${Date.now()}-${Math.random().toString(36).substring(7)}.${fileExt}`;

                const { data: uploadData, error: uploadError } = await supabase.storage
                    .from('package-images') // The bucket you just created
                    .upload(`public/${fileName}`, imageFile, {
                        cacheControl: '3600',
                        upsert: false
                    });

                if (uploadError) throw new Error("Image upload failed: " + uploadError.message);

                // Get the public URL of the uploaded image
                const { data: publicUrlData } = supabase.storage
                    .from('package-images')
                    .getPublicUrl(`public/${fileName}`);

                finalImageUrl = publicUrlData.publicUrl;
            }

            // 2. Prepare the payload for Spring Boot using the new Supabase URL
            const payload = {
                ...formData,
                durationDays: Number(formData.durationDays),
                basePrice: Number(formData.basePrice),
                discountPercentage: Number(formData.discountPercentage),
                maxParticipants: Number(formData.maxParticipants),
                destinations: formData.destinations.split(",").map(d => d.trim()).filter(d => d !== ""),
                images: [finalImageUrl],
                isFeatured: true,
            };

            // 3. Send it to your Trip Plan Service
            const res = await fetch("http://localhost:8089/api/packages", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload),
            });

            if (!res.ok) throw new Error("Failed to save package in the database.");

            setStatus('success');
            setTimeout(() => router.push("/packages"), 2000);

        } catch (error: any) {
            console.error(error);
            setErrorMessage(error.message || "An unexpected error occurred.");
            setStatus('error');
        }
    };

    return (
        <div className="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
            <div className="max-w-3xl mx-auto bg-white rounded-xl shadow-md border border-gray-200 overflow-hidden">

                <div className="bg-emerald-900 px-8 py-6 text-white flex items-center justify-between">
                    <div>
                        <h1 className="text-2xl font-bold flex items-center"><PlusCircle className="mr-2" /> Create Travel Package</h1>
                        <p className="text-emerald-200 text-sm mt-1">Add a new trip with a custom image upload.</p>
                    </div>
                    <button onClick={() => router.back()} className="text-emerald-200 hover:text-white transition-colors">
                        <ArrowLeft className="w-6 h-6" />
                    </button>
                </div>

                <form onSubmit={handleSubmit} className="p-8 space-y-6">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">

                        {/* Standard Text Inputs */}
                        <div className="md:col-span-2">
                            <label className="block text-sm font-medium text-gray-700 mb-1">Package Name</label>
                            <input required type="text" name="name" value={formData.name} onChange={handleChange} className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-emerald-500 focus:border-emerald-500" />
                        </div>

                        <div className="md:col-span-2">
                            <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
                            <textarea required name="description" value={formData.description} onChange={handleChange} rows={3} className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-emerald-500 focus:border-emerald-500" />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">Duration (Days)</label>
                            <input required type="number" min="1" name="durationDays" value={formData.durationDays} onChange={handleChange} className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-emerald-500 focus:border-emerald-500" />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">Base Price (Rs.)</label>
                            <input required type="number" step="0.01" min="0" name="basePrice" value={formData.basePrice} onChange={handleChange} className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-emerald-500 focus:border-emerald-500" />
                        </div>

                        <div className="md:col-span-2">
                            <label className="block text-sm font-medium text-gray-700 mb-1">Destinations (Comma separated)</label>
                            <input type="text" name="destinations" value={formData.destinations} onChange={handleChange} placeholder="e.g., Kandy, Nuwara Eliya" className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-emerald-500 focus:border-emerald-500" />
                        </div>

                        {/* Custom Image Upload Section */}
                        <div className="md:col-span-2">
                            <label className="block text-sm font-medium text-gray-700 mb-2">Upload Package Cover Image</label>
                            <div className="mt-1 flex justify-center px-6 pt-5 pb-6 border-2 border-gray-300 border-dashed rounded-lg hover:bg-gray-50 transition-colors">
                                <div className="space-y-1 text-center">
                                    {imagePreview ? (
                                        <img src={imagePreview} alt="Preview" className="mx-auto h-32 object-cover rounded-md mb-4 shadow-sm" />
                                    ) : (
                                        <UploadCloud className="mx-auto h-12 w-12 text-gray-400" />
                                    )}
                                    <div className="flex text-sm text-gray-600 justify-center">
                                        <label htmlFor="file-upload" className="relative cursor-pointer bg-white rounded-md font-medium text-emerald-600 hover:text-emerald-500 focus-within:outline-none focus-within:ring-2 focus-within:ring-offset-2 focus-within:ring-emerald-500">
                                            <span>Upload a file</span>
                                            <input id="file-upload" name="file-upload" type="file" accept="image/*" className="sr-only" onChange={handleFileChange} />
                                        </label>
                                        <p className="pl-1">or drag and drop</p>
                                    </div>
                                    <p className="text-xs text-gray-500">PNG, JPG, GIF up to 5MB</p>
                                </div>
                            </div>
                        </div>
                    </div>

                    {status === 'error' && <div className="p-4 bg-red-50 text-red-700 rounded-lg border border-red-200">{errorMessage}</div>}
                    {status === 'success' && <div className="p-4 bg-emerald-50 text-emerald-800 rounded-lg border border-emerald-200 flex items-center"><CheckCircle className="w-5 h-5 mr-2" /> Uploaded and created successfully! Redirecting...</div>}

                    <div className="pt-4 border-t border-gray-100">
                        <button type="submit" disabled={status === 'loading' || status === 'success'} className="w-full flex justify-center py-3 px-4 border border-transparent rounded-lg shadow-sm text-lg font-medium text-white bg-emerald-600 hover:bg-emerald-700 disabled:opacity-70">
                            {status === 'loading' ? <><Loader2 className="w-6 h-6 mr-2 animate-spin" /> Uploading Image & Saving...</> : 'Save Travel Package'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
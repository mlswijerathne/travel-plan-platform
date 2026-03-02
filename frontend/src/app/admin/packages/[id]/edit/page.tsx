"use client";

import React, { useState, useEffect } from "react";
import { useRouter, useParams } from "next/navigation";
import { ArrowLeft, Loader2, Edit, CheckCircle, UploadCloud } from "lucide-react";
import { createClient } from "@supabase/supabase-js";

// Initialize Supabase client safely using environment variables
const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL as string;
const supabaseKey = process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY as string;
const supabase = createClient(supabaseUrl, supabaseKey);

export default function EditPackagePage() {
    const router = useRouter();
    const { id } = useParams();

    const [formData, setFormData] = useState({
        name: "",
        description: "",
        durationDays: 1,
        basePrice: 0,
        discountPercentage: 0,
        maxParticipants: 10,
        destinations: "",
        existingImageUrl: "", // Keeps track of the image already in the DB
    });

    const [imageFile, setImageFile] = useState<File | null>(null);
    const [imagePreview, setImagePreview] = useState<string | null>(null);

    const [status, setStatus] = useState<'loading_data' | 'idle' | 'saving' | 'success' | 'error'>('loading_data');
    const [errorMessage, setErrorMessage] = useState("");

    // Fetch existing package data when the page loads
    useEffect(() => {
        if (!id) return;

        fetch(`http://localhost:8089/api/packages/${id}`)
            .then(res => {
                if (!res.ok) throw new Error("Failed to fetch package details");
                return res.json();
            })
            .then(json => {
                const pkg = json.data;
                setFormData({
                    name: pkg.name,
                    description: pkg.description,
                    durationDays: pkg.durationDays,
                    basePrice: pkg.basePrice,
                    discountPercentage: pkg.discountPercentage,
                    maxParticipants: pkg.maxParticipants,
                    destinations: pkg.destinations ? pkg.destinations.join(", ") : "",
                    existingImageUrl: pkg.images && pkg.images.length > 0 ? pkg.images[0] : "",
                });
                if (pkg.images && pkg.images.length > 0) {
                    setImagePreview(pkg.images[0]); // Show the existing image as preview
                }
                setStatus('idle');
            })
            .catch(err => {
                console.error(err);
                setErrorMessage("Could not load package data.");
                setStatus('error');
            });
    }, [id]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files.length > 0) {
            const file = e.target.files[0];
            setImageFile(file);
            setImagePreview(URL.createObjectURL(file));
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setStatus('saving');
        setErrorMessage("");

        try {
            // Default to the old image if no new one is uploaded
            let finalImageUrl = formData.existingImageUrl;

            // 1. If the admin selected a NEW file, upload it to Supabase
            if (imageFile) {
                const fileExt = imageFile.name.split('.').pop();
                const fileName = `${Date.now()}-${Math.random().toString(36).substring(7)}.${fileExt}`;

                const { error: uploadError } = await supabase.storage
                    .from('package-images')
                    .upload(`public/${fileName}`, imageFile, { cacheControl: '3600', upsert: false });

                if (uploadError) throw new Error("Image upload failed: " + uploadError.message);

                const { data: publicUrlData } = supabase.storage
                    .from('package-images')
                    .getPublicUrl(`public/${fileName}`);

                finalImageUrl = publicUrlData.publicUrl;
            }

            // 2. Prepare payload exactly how Spring Boot expects it
            const payload = {
                ...formData,
                durationDays: Number(formData.durationDays),
                basePrice: Number(formData.basePrice),
                discountPercentage: Number(formData.discountPercentage),
                maxParticipants: Number(formData.maxParticipants),
                destinations: formData.destinations.split(",").map(d => d.trim()).filter(d => d !== ""),
                images: finalImageUrl ? [finalImageUrl] : [],
                isFeatured: true,
            };

            // 3. Send PUT request to update the record in Spring Boot
            const res = await fetch(`http://localhost:8089/api/packages/${id}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload),
            });

            if (!res.ok) {
                let errText = "Failed to update package in the database.";
                try {
                    const text = await res.text();
                    errText += ` Details: ${text}`;
                } catch (e) { }
                throw new Error(errText);
            }

            setStatus('success');

            // Redirect back to the package details page after success
            setTimeout(() => router.push(`/packages/${id}`), 2000);

        } catch (error: any) {
            console.error(error);
            setErrorMessage(error.message || "An unexpected error occurred.");
            setStatus('error');
        }
    };

    if (status === 'loading_data') {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gray-50">
                <Loader2 className="w-10 h-10 animate-spin text-emerald-600" />
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
            <div className="max-w-3xl mx-auto bg-white rounded-xl shadow-md border border-gray-200 overflow-hidden">

                {/* Header */}
                <div className="bg-emerald-900 px-8 py-6 text-white flex items-center justify-between">
                    <div>
                        <h1 className="text-2xl font-bold flex items-center"><Edit className="mr-2" /> Edit Travel Package</h1>
                        <p className="text-emerald-200 text-sm mt-1">Update details for this trip.</p>
                    </div>
                    <button onClick={() => router.back()} className="text-emerald-200 hover:text-white transition-colors">
                        <ArrowLeft className="w-6 h-6" />
                    </button>
                </div>

                {/* Form */}
                <form onSubmit={handleSubmit} className="p-8 space-y-6">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
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

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">Discount Percentage (%)</label>
                            <input required type="number" step="0.1" min="0" max="100" name="discountPercentage" value={formData.discountPercentage} onChange={handleChange} className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-emerald-500 focus:border-emerald-500" />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">Max Participants</label>
                            <input required type="number" min="1" name="maxParticipants" value={formData.maxParticipants} onChange={handleChange} className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-emerald-500 focus:border-emerald-500" />
                        </div>

                        <div className="md:col-span-2">
                            <label className="block text-sm font-medium text-gray-700 mb-1">Destinations (Comma separated)</label>
                            <input type="text" name="destinations" value={formData.destinations} onChange={handleChange} className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-emerald-500 focus:border-emerald-500" />
                        </div>

                        {/* Image Upload Area */}
                        <div className="md:col-span-2">
                            <label className="block text-sm font-medium text-gray-700 mb-2">Update Cover Image (Optional)</label>
                            <div className="mt-1 flex justify-center px-6 pt-5 pb-6 border-2 border-gray-300 border-dashed rounded-lg hover:bg-gray-50 transition-colors">
                                <div className="space-y-1 text-center">
                                    {imagePreview ? (
                                        <img src={imagePreview} alt="Preview" className="mx-auto h-32 object-cover rounded-md mb-4 shadow-sm" />
                                    ) : (
                                        <UploadCloud className="mx-auto h-12 w-12 text-gray-400" />
                                    )}
                                    <div className="flex text-sm text-gray-600 justify-center">
                                        <label htmlFor="file-upload" className="relative cursor-pointer bg-white rounded-md font-medium text-emerald-600 hover:text-emerald-500 focus-within:outline-none focus-within:ring-2 focus-within:ring-offset-2 focus-within:ring-emerald-500">
                                            <span>Upload new file</span>
                                            <input id="file-upload" name="file-upload" type="file" accept="image/*" className="sr-only" onChange={handleFileChange} />
                                        </label>
                                        <p className="pl-1">to replace the current image</p>
                                    </div>
                                    <p className="text-xs text-gray-500">Leave this empty to keep the existing image.</p>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Status Messages */}
                    {status === 'error' && <div className="p-4 bg-red-50 text-red-700 rounded-lg border border-red-200">{errorMessage}</div>}
                    {status === 'success' && <div className="p-4 bg-emerald-50 text-emerald-800 rounded-lg border border-emerald-200 flex items-center"><CheckCircle className="w-5 h-5 mr-2" /> Package updated successfully! Redirecting...</div>}

                    {/* Submit Button */}
                    <div className="pt-4 border-t border-gray-100">
                        <button type="submit" disabled={status === 'saving' || status === 'success'} className="w-full flex justify-center py-3 px-4 border border-transparent rounded-lg shadow-sm text-lg font-medium text-white bg-emerald-600 hover:bg-emerald-700 disabled:opacity-70">
                            {status === 'saving' ? <><Loader2 className="w-6 h-6 mr-2 animate-spin" /> Updating Package...</> : 'Update Travel Package'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
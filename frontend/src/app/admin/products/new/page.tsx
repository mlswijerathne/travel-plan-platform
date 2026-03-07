"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Textarea } from "@/components/ui/textarea";
import { createClient } from "@/lib/supabase/client";
import { uploadImage } from "@/lib/api/images";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8060';

export default function AddProductPage() {
    const router = useRouter();
    const [loading, setLoading] = useState(false);
    const [imageFile, setImageFile] = useState<File | null>(null);

    const [formData, setFormData] = useState({
        name: "",
        category: "Souvenirs",
        description: "",
        price: "",
        stockQuantity: "10",
    });

    const handleChange = (e: any) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e: any) => {
        e.preventDefault();
        setLoading(true);

        try {
            const supabase = createClient();
            const { data: { session } } = await supabase.auth.getSession();
            const authHeaders: Record<string, string> = {};
            if (session?.access_token) {
                authHeaders['Authorization'] = `Bearer ${session.access_token}`;
            }

            let imageUrl = "";
            if (imageFile) {
                imageUrl = await uploadImage(imageFile, "products");
            }

            const productPayload = { ...formData, imageUrl };
            const data = new FormData();
            data.append("product", new Blob([JSON.stringify(productPayload)], { type: "application/json" }));

            const res = await fetch(`${API_BASE}/api/products`, {
                method: "POST",
                headers: authHeaders,
                body: data,
            });

            if (res.ok) {
                alert("Product successfully added to the shop!");
                router.push("/admin/products");
            } else {
                const errorText = await res.text();
                alert(`Failed to add product: ${errorText}`);
            }
        } catch (error) {
            console.error(error);
            alert("Error uploading product. Make sure the backend is running.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="flex justify-center">
                <Card className="w-full max-w-lg shadow-xl border-none">
                    <CardHeader className="bg-emerald-900 text-white rounded-t-xl">
                        <CardTitle className="text-2xl">List New Product</CardTitle>
                    </CardHeader>
                    <CardContent className="pt-6">
                        <form onSubmit={handleSubmit} className="space-y-4">

                            {/* Image Upload */}
                            <div className="space-y-2">
                                <Label className="font-bold text-gray-700">Product Image</Label>
                                <Input
                                    type="file"
                                    accept="image/*"
                                    onChange={(e) => e.target.files && setImageFile(e.target.files[0])}
                                    required
                                    className="cursor-pointer"
                                />
                            </div>

                            {/* Name & Category */}
                            <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-2">
                                    <Label className="font-bold text-gray-700">Product Name</Label>
                                    <Input name="name" placeholder="e.g. Ceylon Tea" onChange={handleChange} required />
                                </div>
                                <div className="space-y-2">
                                    <Label className="font-bold text-gray-700">Category</Label>
                                    <Input name="category" placeholder="Souvenirs" defaultValue="Souvenirs" onChange={handleChange} required />
                                </div>
                            </div>

                            {/* Price & Stock */}
                            <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-2">
                                    <Label className="font-bold text-gray-700">Price ($)</Label>
                                    <Input type="number" step="0.01" name="price" placeholder="15.00" onChange={handleChange} required />
                                </div>
                                <div className="space-y-2">
                                    <Label className="font-bold text-gray-700">Stock Quantity</Label>
                                    <Input type="number" name="stockQuantity" defaultValue={10} onChange={handleChange} required />
                                </div>
                            </div>

                            {/* Description */}
                            <div className="space-y-2">
                                <Label className="font-bold text-gray-700">Description</Label>
                                <Textarea name="description" placeholder="Describe the item..." onChange={handleChange} required />
                            </div>

                            <Button type="submit" className="w-full bg-emerald-600 hover:bg-emerald-700 h-12 text-lg mt-4" disabled={loading}>
                                {loading ? "Adding Product..." : "Add Product"}
                            </Button>
                        </form>
                    </CardContent>
                </Card>
        </div>
    );
}

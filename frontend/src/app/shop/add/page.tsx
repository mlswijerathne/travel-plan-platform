"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Navbar } from "@/components/Navbar";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Textarea } from "@/components/ui/textarea";

export default function AddProductPage() {
    const router = useRouter();
    const [loading, setLoading] = useState(false);
    const [imageFile, setImageFile] = useState<File | null>(null);

    const [formData, setFormData] = useState({
        name: "",
        category: "Souvenirs", // Default category
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
            const data = new FormData();

            // The backend expects the product details as a JSON string named "product"
            data.append("product", new Blob([JSON.stringify(formData)], { type: "application/json" }));

            // The backend expects the file named "image"
            if (imageFile) {
                data.append("image", imageFile);
            }

            // POST to E-Commerce Service (Port 8091)
            const res = await fetch("http://localhost:8091/api/products", {
                method: "POST",
                body: data,
            });

            if (res.ok) {
                alert("Product successfully added to the shop!");
                router.push("/shop"); // Redirect back to the main shop page
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
        <div className="min-h-screen bg-gray-50">
            <Navbar />
            <div className="container mx-auto px-4 py-12 flex justify-center">
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
        </div>
    );
}
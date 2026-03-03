"use client";

import React, { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Card, CardHeader, CardTitle } from "@/components/ui/card";
import { ShoppingBag } from "lucide-react";
import { getProduct } from "@/lib/api/products";
import { createOrder } from "@/lib/api/orders";

export default function BuyProductPage({ params }: { params: Promise<{ id: string }> }) {
    const router = useRouter();
    const { id } = React.use(params);

    const [product, setProduct] = useState<any>(null);
    const [loading, setLoading] = useState(false);
    const [formData, setFormData] = useState({
        quantity: 1,
        deliveryType: "LOCAL_COLLECTION", // Default enum value
        hotelId: "", // Only used if HOTEL_DROP_OFF
    });

    useEffect(() => {
        getProduct(Number(id))
            .then((data) => setProduct(data))
            .catch((err) => console.error("Failed to load product:", err));
    }, [id]);

    const handleSubmit = async (e: any) => {
        e.preventDefault();
        setLoading(true);

        // This matches your new OrderRequestDTO perfectly!
        const orderData = {
            deliveryType: formData.deliveryType,
            deliveryLocationId: formData.deliveryType === "HOTEL_DROP_OFF" ? formData.hotelId : null,
            itineraryId: null, // Can be added later when integrating with Itinerary Service
            items: [
                {
                    productId: product.id,
                    quantity: formData.quantity,
                }
            ]
        };

        try {
            await createOrder(orderData as any);
            alert("Purchase Successful! Thank you for your order.");
            router.push("/shop");
        } catch (err: any) {
            console.error(err);
            alert("Failed to purchase: " + (err.message || "Something went wrong."));
        } finally {
            setLoading(false);
        }
    };

    if (!product) return <div className="text-center py-20 animate-pulse text-emerald-700">Loading checkout...</div>;

    return (
        <div className="flex justify-center">
                <Card className="w-full max-w-4xl grid grid-cols-1 md:grid-cols-2 shadow-xl overflow-hidden border-none">

                    {/* LEFT SIDE: Product Summary */}
                    <div className="bg-emerald-900 text-white p-8 flex flex-col justify-between">
                        <div>
                            <h2 className="text-3xl font-bold mb-2">{product.name}</h2>
                            <p className="text-emerald-200 mb-6">{product.category}</p>

                            <div className="h-64 bg-white/10 rounded-lg mb-6 overflow-hidden flex items-center justify-center">
                                {product.imageUrl ? (
                                    <img src={product.imageUrl} alt={product.name} className="w-full h-full object-cover" />
                                ) : (
                                    <span className="text-emerald-300">No Image Available</span>
                                )}
                            </div>
                            <p className="text-emerald-100">{product.description}</p>
                        </div>

                        <div className="border-t border-emerald-700 pt-4 mt-6">
                            <div className="flex justify-between items-center text-lg mb-2">
                                <span>Price per item:</span>
                                <span>Rs. {product.price}</span>
                            </div>
                            <div className="flex justify-between items-center text-2xl font-bold text-white">
                                <span>Total:</span>
                                <span>Rs. {(product.price * formData.quantity).toFixed(2)}</span>
                            </div>
                        </div>
                    </div>

                    {/* RIGHT SIDE: Checkout Form */}
                    <div className="p-8 bg-white">
                        <CardHeader className="px-0 pt-0">
                            <CardTitle className="text-2xl text-gray-800">Complete Purchase</CardTitle>
                        </CardHeader>
                        <form onSubmit={handleSubmit} className="space-y-6 mt-4">

                            <div className="space-y-2">
                                <Label className="font-bold text-gray-700">Quantity</Label>
                                <Input
                                    type="number"
                                    min="1"
                                    max={product.stockQuantity}
                                    value={formData.quantity}
                                    onChange={(e) => setFormData({ ...formData, quantity: parseInt(e.target.value) || 1 })}
                                    required
                                />
                                <p className="text-sm text-gray-500">{product.stockQuantity} items currently in stock</p>
                            </div>

                            <div className="space-y-2">
                                <Label className="font-bold text-gray-700">Delivery Method</Label>
                                <select
                                    className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                                    value={formData.deliveryType}
                                    onChange={(e) => setFormData({ ...formData, deliveryType: e.target.value })}
                                >
                                    <option value="LOCAL_COLLECTION">Store Pickup</option>
                                    <option value="AIRPORT_PICKUP">Airport Pickup</option>
                                    {/* Note: HOTEL_DROP_OFF will require the Hotel Service running on port 8083 to work! */}
                                    <option value="HOTEL_DROP_OFF">Hotel Drop-off</option>
                                </select>
                            </div>

                            {formData.deliveryType === "HOTEL_DROP_OFF" && (
                                <div className="space-y-2">
                                    <Label className="font-bold text-gray-700">Hotel ID</Label>
                                    <Input
                                        placeholder="e.g. hotel-uuid-123"
                                        value={formData.hotelId}
                                        onChange={(e) => setFormData({ ...formData, hotelId: e.target.value })}
                                        required
                                    />
                                    <p className="text-xs text-orange-600">Note: Hotel Service (8083) must be running to verify this ID.</p>
                                </div>
                            )}

                            <Button className="w-full bg-emerald-600 hover:bg-emerald-700 h-14 text-lg font-bold shadow-md" disabled={loading}>
                                <ShoppingBag className="mr-2 h-5 w-5" />
                                {loading ? "Processing Securely..." : "Confirm Purchase"}
                            </Button>
                        </form>
                    </div>
                </Card>
        </div>
    );
}
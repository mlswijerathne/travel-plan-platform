export interface PackageItem {
    id: number;
    dayNumber: number;
    providerType: string;
    providerId: number;
    itemName: string;
    description: string;
    sortOrder: number;
    createdAt: string;
}

export interface TripPackage {
    id: number;
    name: string;
    description: string;
    durationDays: number;
    basePrice: number;
    discountPercentage: number;
    finalPrice?: number;
    maxParticipants: number;
    destinations: string[];
    inclusions: string[];
    exclusions: string[];
    images: string[];
    isFeatured?: boolean;
    isActive?: boolean;
    items?: PackageItem[];
}

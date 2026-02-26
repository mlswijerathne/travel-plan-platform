import Link from "next/link";
import { Button } from "@/components/ui/button";

export function Navbar() {
    return (
        <nav className="border-b bg-white">
            <div className="container mx-auto px-4 h-16 flex items-center justify-between">
                {/* Logo */}
                <Link href="/" className="text-2xl font-bold text-primary">
                    Travel<span className="text-black">Plan</span>
                </Link>

                {/* Navigation Links */}
                <div className="hidden md:flex gap-8 text-sm font-medium text-gray-600 items-center">
                    <Link href="/vehicles" className="hover:text-black transition">
                        Vehicles
                    </Link>
                    <Link href="/about" className="hover:text-black transition">
                        About
                    </Link>
                    <Link href="/contact" className="hover:text-black transition">
                        Contact
                    </Link>

                    {/* NEW ADMIN LINK */}
                    <Link href="/admin" className="text-red-600 font-bold hover:text-red-800 transition">
                        Admin Dashboard
                    </Link>
                </div>

                {/* Auth Buttons & Actions */}
                <div className="flex gap-4 items-center">
                    {/* Link to Add Vehicle Page */}
                    <Link href="/vehicles/add">
                        <Button className="bg-black text-white hover:bg-gray-800">
                            + List My Car
                        </Button>
                    </Link>

                    <Button variant="outline">Log in</Button>
                </div>
            </div>
        </nav>
    );
}
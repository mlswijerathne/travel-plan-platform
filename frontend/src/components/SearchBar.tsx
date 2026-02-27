"use client";

import { useState } from "react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Search } from "lucide-react";

interface SearchProps {
    onSearch: (query: string) => void;
}

export function SearchBar({ onSearch }: SearchProps) {
    const [query, setQuery] = useState("");

    const handleSearch = () => {
        onSearch(query);
    };

    // Allow pressing "Enter" to search
    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === "Enter") {
            handleSearch();
        }
    };

    return (
        <div className="flex w-full max-w-sm items-center space-x-2">
            <Input
                type="text"
                placeholder="Search make or model..."
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                onKeyDown={handleKeyDown}
            />
            <Button type="submit" onClick={handleSearch}>
                <Search className="mr-2 h-4 w-4" /> Search
            </Button>
        </div>
    );
}
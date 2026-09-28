package com.wayfarer.catalog.domain;

import java.util.List;
import java.util.Objects;

public record RoomOffer(
        String id,
        String name,
        String description,
        String bedSummary,
        int maxGuests,
        long pricePerNightCents,
        String currency,
        List<String> amenities) {

    public RoomOffer {
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);
        Objects.requireNonNull(description);
        Objects.requireNonNull(bedSummary);
        Objects.requireNonNull(currency);
        amenities = List.copyOf(amenities);
        if (maxGuests < 1 || pricePerNightCents < 0) {
            throw new IllegalArgumentException("Room capacity and price must be positive.");
        }
    }
}

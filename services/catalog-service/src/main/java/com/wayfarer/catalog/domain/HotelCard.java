package com.wayfarer.catalog.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record HotelCard(
        String id,
        String name,
        String city,
        String country,
        String address,
        int starRating,
        BigDecimal guestRating,
        int reviewCount,
        long priceFromCents,
        String currency,
        String tagline,
        String imageUrl,
        List<String> highlights,
        boolean featured) {

    public HotelCard {
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);
        Objects.requireNonNull(city);
        Objects.requireNonNull(country);
        Objects.requireNonNull(guestRating);
        Objects.requireNonNull(currency);
        Objects.requireNonNull(imageUrl);
        highlights = List.copyOf(highlights);
        if (starRating < 1 || starRating > 5 || guestRating.signum() < 0
                || guestRating.compareTo(BigDecimal.TEN) > 0 || priceFromCents < 0) {
            throw new IllegalArgumentException("Hotel ratings and prices are outside their allowed range.");
        }
    }
}

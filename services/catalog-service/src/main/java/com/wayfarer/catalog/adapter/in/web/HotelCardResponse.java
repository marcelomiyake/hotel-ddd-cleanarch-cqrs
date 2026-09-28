package com.wayfarer.catalog.adapter.in.web;

import com.wayfarer.catalog.domain.HotelCard;

import java.math.BigDecimal;
import java.util.List;

public record HotelCardResponse(
        String id, String name, String city, String country, String address, int starRating,
        BigDecimal guestRating, int reviewCount, long priceFromCents, String currency,
        String tagline, String imageUrl, List<String> highlights, boolean featured) {
    public static HotelCardResponse from(HotelCard hotel) {
        return new HotelCardResponse(hotel.id(), hotel.name(), hotel.city(), hotel.country(),
                hotel.address(), hotel.starRating(), hotel.guestRating(), hotel.reviewCount(),
                hotel.priceFromCents(), hotel.currency(), hotel.tagline(), hotel.imageUrl(),
                hotel.highlights(), hotel.featured());
    }
}

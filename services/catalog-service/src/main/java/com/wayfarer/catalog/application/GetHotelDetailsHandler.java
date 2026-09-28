package com.wayfarer.catalog.application;

import com.wayfarer.catalog.domain.HotelDetails;
import com.wayfarer.catalog.domain.HotelNotFoundException;

import java.util.Objects;

public final class GetHotelDetailsHandler {
    private final HotelCatalog catalog;

    public GetHotelDetailsHandler(HotelCatalog catalog) {
        this.catalog = Objects.requireNonNull(catalog);
    }

    public HotelDetails handle(String hotelId) {
        if (hotelId == null || hotelId.isBlank()) {
            throw new IllegalArgumentException("Hotel id is required.");
        }
        return catalog.findById(hotelId.trim()).orElseThrow(() -> new HotelNotFoundException(hotelId));
    }
}

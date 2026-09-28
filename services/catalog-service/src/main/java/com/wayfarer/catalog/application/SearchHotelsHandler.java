package com.wayfarer.catalog.application;

import com.wayfarer.catalog.domain.HotelCard;

import java.util.List;
import java.util.Objects;

public final class SearchHotelsHandler {
    private final HotelCatalog catalog;

    public SearchHotelsHandler(HotelCatalog catalog) {
        this.catalog = Objects.requireNonNull(catalog);
    }

    public List<HotelCard> handle(SearchHotelsQuery query) {
        Objects.requireNonNull(query);
        return List.copyOf(catalog.search(query.city()));
    }
}

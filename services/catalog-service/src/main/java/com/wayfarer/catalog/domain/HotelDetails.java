package com.wayfarer.catalog.domain;

import java.util.List;
import java.util.Objects;

public record HotelDetails(
        HotelCard hotel,
        String description,
        String locationSummary,
        List<String> gallery,
        List<RoomOffer> rooms) {

    public HotelDetails {
        Objects.requireNonNull(hotel);
        Objects.requireNonNull(description);
        Objects.requireNonNull(locationSummary);
        gallery = List.copyOf(gallery);
        rooms = List.copyOf(rooms);
    }
}

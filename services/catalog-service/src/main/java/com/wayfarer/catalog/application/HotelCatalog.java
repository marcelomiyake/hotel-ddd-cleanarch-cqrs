package com.wayfarer.catalog.application;

import com.wayfarer.catalog.domain.HotelCard;
import com.wayfarer.catalog.domain.HotelDetails;

import java.util.List;
import java.util.Optional;

public interface HotelCatalog {
    List<HotelCard> search(String city);

    Optional<HotelDetails> findById(String hotelId);
}

package com.wayfarer.catalog.adapter.in.web;

import com.wayfarer.catalog.domain.HotelDetails;
import com.wayfarer.catalog.domain.RoomOffer;

import java.util.List;

public record HotelDetailsResponse(
        HotelCardResponse hotel, String description, String locationSummary,
        List<String> gallery, List<RoomTypeResponse> rooms) {
    public HotelDetailsResponse {
        gallery = List.copyOf(gallery);
        rooms = List.copyOf(rooms);
    }

    public static HotelDetailsResponse from(HotelDetails details) {
        return new HotelDetailsResponse(
                HotelCardResponse.from(details.hotel()), details.description(), details.locationSummary(),
                details.gallery(), details.rooms().stream().map(RoomTypeResponse::from).toList());
    }

    public record RoomTypeResponse(
            String id, String name, String description, String bedSummary, int maxGuests,
            long pricePerNightCents, String currency, List<String> amenities) {
        public RoomTypeResponse {
            amenities = List.copyOf(amenities);
        }

        private static RoomTypeResponse from(RoomOffer room) {
            return new RoomTypeResponse(room.id(), room.name(), room.description(), room.bedSummary(),
                    room.maxGuests(), room.pricePerNightCents(), room.currency(), room.amenities());
        }
    }
}

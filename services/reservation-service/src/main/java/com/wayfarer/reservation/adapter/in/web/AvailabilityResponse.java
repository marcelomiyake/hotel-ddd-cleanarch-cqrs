package com.wayfarer.reservation.adapter.in.web;

import com.wayfarer.reservation.domain.RoomAvailability;

public record AvailabilityResponse(String hotelId, String roomTypeId, String roomName,
        int availableRooms, int maxGuestsPerRoom, long pricePerNightCents, String currency) {
    public static AvailabilityResponse from(RoomAvailability offer) {
        return new AvailabilityResponse(offer.hotelId(), offer.roomTypeId(), offer.roomName(),
                offer.availableRooms(), offer.maxGuestsPerRoom(), offer.pricePerNightCents(), offer.currency());
    }
}

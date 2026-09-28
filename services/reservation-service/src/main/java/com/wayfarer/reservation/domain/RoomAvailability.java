package com.wayfarer.reservation.domain;

import java.util.Objects;

public record RoomAvailability(
        String hotelId,
        String roomTypeId,
        String roomName,
        int availableRooms,
        int maxGuestsPerRoom,
        long pricePerNightCents,
        String currency) {

    public RoomAvailability {
        Objects.requireNonNull(hotelId);
        Objects.requireNonNull(roomTypeId);
        Objects.requireNonNull(roomName);
        Objects.requireNonNull(currency);
        if (availableRooms < 0 || maxGuestsPerRoom < 1 || pricePerNightCents < 0) {
            throw new IllegalArgumentException("Inventory, room capacity, or price is invalid.");
        }
    }

    public boolean canAccommodate(int guests, int rooms) {
        return guests > 0 && rooms > 0
                && rooms <= availableRooms
                && guests <= Math.multiplyExact(maxGuestsPerRoom, rooms);
    }

    public long totalPriceCents(StayPeriod stay, int rooms) {
        return Math.multiplyExact(Math.multiplyExact(pricePerNightCents, stay.nights()), rooms);
    }
}

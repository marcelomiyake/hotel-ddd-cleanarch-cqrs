package com.wayfarer.reservation.application;

import java.time.LocalDate;

public record PlaceReservationCommand(
        String idempotencyKey,
        String hotelId,
        String roomTypeId,
        String guestName,
        String guestEmail,
        LocalDate checkIn,
        LocalDate checkOut,
        int guests,
        int rooms) {
}

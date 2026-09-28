package com.wayfarer.reservation.adapter.in.web;

import com.wayfarer.reservation.domain.Reservation;
import com.wayfarer.reservation.domain.ReservationStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ReservationResponse(UUID id, String hotelId, String roomTypeId, String roomName,
        String guestName, String guestEmail, LocalDate checkIn, LocalDate checkOut, int nights,
        int guests, int rooms, ReservationStatus status, long pricePerNightCents,
        long totalPriceCents, String currency, Instant createdAt) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(reservation.id(), reservation.hotelId(), reservation.roomTypeId(),
                reservation.roomName(), reservation.guest().fullName(), reservation.guest().email(),
                reservation.stay().checkIn(), reservation.stay().checkOut(), reservation.stay().nights(),
                reservation.guests(), reservation.rooms(), reservation.status(),
                reservation.pricePerNightCents(), reservation.totalPriceCents(), reservation.currency(),
                reservation.createdAt());
    }
}

package com.wayfarer.reservation.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record Reservation(
        UUID id,
        String idempotencyKey,
        String hotelId,
        String roomTypeId,
        String roomName,
        GuestDetails guest,
        StayPeriod stay,
        int guests,
        int rooms,
        ReservationStatus status,
        long pricePerNightCents,
        long totalPriceCents,
        String currency,
        Instant createdAt) {

    public Reservation {
        Objects.requireNonNull(id);
        Objects.requireNonNull(idempotencyKey);
        Objects.requireNonNull(hotelId);
        Objects.requireNonNull(roomTypeId);
        Objects.requireNonNull(roomName);
        Objects.requireNonNull(guest);
        Objects.requireNonNull(stay);
        Objects.requireNonNull(status);
        Objects.requireNonNull(currency);
        Objects.requireNonNull(createdAt);
        if (idempotencyKey.isBlank() || idempotencyKey.length() > 200 || guests < 1 || rooms < 1
                || pricePerNightCents < 0 || totalPriceCents < 0) {
            throw new DomainRuleViolation("Reservation details are invalid.");
        }
    }

    public static Reservation place(
            UUID id,
            String idempotencyKey,
            String hotelId,
            String roomTypeId,
            GuestDetails guest,
            StayPeriod stay,
            int guests,
            int rooms,
            RoomAvailability offer,
            LocalDate today,
            Instant createdAt) {
        Objects.requireNonNull(offer);
        Objects.requireNonNull(today);
        if (!offer.hotelId().equals(hotelId) || !offer.roomTypeId().equals(roomTypeId)) {
            throw new DomainRuleViolation("The room does not belong to the selected hotel.");
        }
        if (stay.startsBefore(today)) {
            throw new DomainRuleViolation("Check-in cannot be in the past.");
        }
        if (!offer.canAccommodate(guests, rooms)) {
            throw new RoomUnavailableException(roomTypeId);
        }
        long totalPrice = offer.totalPriceCents(stay, rooms);
        return new Reservation(id, idempotencyKey, hotelId, roomTypeId, offer.roomName(), guest, stay,
                guests, rooms, ReservationStatus.CONFIRMED, offer.pricePerNightCents(), totalPrice,
                offer.currency(), createdAt);
    }

    public Reservation cancel(LocalDate today) {
        if (status != ReservationStatus.CONFIRMED) {
            throw new CancellationNotAllowedException("Only a confirmed reservation can be cancelled.");
        }
        if (!stay.checkIn().isAfter(today)) {
            throw new CancellationNotAllowedException("Reservations can only be cancelled before check-in day.");
        }
        return new Reservation(id, idempotencyKey, hotelId, roomTypeId, roomName, guest, stay, guests, rooms,
                ReservationStatus.CANCELLED, pricePerNightCents, totalPriceCents, currency, createdAt);
    }

    public boolean matches(String requestedHotelId, String requestedRoomTypeId, GuestDetails requestedGuest,
                           StayPeriod requestedStay, int requestedGuests, int requestedRooms) {
        return hotelId.equals(requestedHotelId) && roomTypeId.equals(requestedRoomTypeId)
                && guest.equals(requestedGuest) && stay.equals(requestedStay)
                && guests == requestedGuests && rooms == requestedRooms;
    }
}

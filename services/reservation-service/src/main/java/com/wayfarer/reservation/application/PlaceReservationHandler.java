package com.wayfarer.reservation.application;

import com.wayfarer.reservation.domain.DomainRuleViolation;
import com.wayfarer.reservation.domain.GuestDetails;
import com.wayfarer.reservation.domain.IdempotencyConflictException;
import com.wayfarer.reservation.domain.Reservation;
import com.wayfarer.reservation.domain.RoomAvailability;
import com.wayfarer.reservation.domain.RoomUnavailableException;
import com.wayfarer.reservation.domain.StayPeriod;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

public final class PlaceReservationHandler {
    private final ReservationStore reservations;
    private final RoomInventory inventory;
    private final TransactionBoundary transactions;
    private final ReservationIdGenerator ids;
    private final Clock clock;

    public PlaceReservationHandler(ReservationStore reservations, RoomInventory inventory,
                                   TransactionBoundary transactions, ReservationIdGenerator ids, Clock clock) {
        this.reservations = Objects.requireNonNull(reservations);
        this.inventory = Objects.requireNonNull(inventory);
        this.transactions = Objects.requireNonNull(transactions);
        this.ids = Objects.requireNonNull(ids);
        this.clock = Objects.requireNonNull(clock);
    }

    public Reservation handle(PlaceReservationCommand command) {
        Objects.requireNonNull(command);
        return transactions.inTransaction(() -> place(command));
    }

    private Reservation place(PlaceReservationCommand command) {
        GuestDetails guest = new GuestDetails(command.guestName(), command.guestEmail());
        StayPeriod stay = new StayPeriod(command.checkIn(), command.checkOut());
        if (command.idempotencyKey() == null || command.idempotencyKey().isBlank()) {
            throw new DomainRuleViolation("An idempotency key is required.");
        }
        if (command.checkIn().isBefore(LocalDate.now(clock))) {
            throw new DomainRuleViolation("Check-in cannot be in the past.");
        }
        reservations.lockIdempotencyKey(command.idempotencyKey().trim());
        var existing = reservations.findByIdempotencyKey(command.idempotencyKey().trim());
        if (existing.isPresent()) {
            Reservation saved = existing.get();
            if (saved.matches(command.hotelId(), command.roomTypeId(), guest, stay, command.guests(), command.rooms())) {
                return saved;
            }
            throw new IdempotencyConflictException();
        }
        RoomAvailability offer = inventory.findAvailability(command.roomTypeId(), stay)
                .orElseThrow(() -> new RoomUnavailableException(command.roomTypeId()));
        if (!offer.canAccommodate(command.guests(), command.rooms())) {
            throw new RoomUnavailableException(command.roomTypeId());
        }
        Reservation reservation = Reservation.place(ids.nextId(), command.idempotencyKey().trim(),
                command.hotelId(), command.roomTypeId(), guest, stay, command.guests(), command.rooms(),
                offer, LocalDate.now(clock), Instant.now(clock));
        inventory.reserve(command.roomTypeId(), stay, command.rooms());
        return reservations.save(reservation);
    }
}

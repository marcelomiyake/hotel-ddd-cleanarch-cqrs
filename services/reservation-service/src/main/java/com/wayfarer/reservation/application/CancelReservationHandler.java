package com.wayfarer.reservation.application;

import com.wayfarer.reservation.domain.Reservation;
import com.wayfarer.reservation.domain.ReservationAccessDeniedException;
import com.wayfarer.reservation.domain.ReservationNotFoundException;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public final class CancelReservationHandler {
    private final ReservationQueries queries;
    private final ReservationStore reservations;
    private final RoomInventory inventory;
    private final TransactionBoundary transactions;
    private final Clock clock;

    public CancelReservationHandler(ReservationQueries queries, ReservationStore reservations,
                                    RoomInventory inventory, TransactionBoundary transactions, Clock clock) {
        this.queries = Objects.requireNonNull(queries);
        this.reservations = Objects.requireNonNull(reservations);
        this.inventory = Objects.requireNonNull(inventory);
        this.transactions = Objects.requireNonNull(transactions);
        this.clock = Objects.requireNonNull(clock);
    }

    public Reservation handle(UUID reservationId, String guestEmail) {
        Objects.requireNonNull(reservationId);
        return transactions.inTransaction(() -> cancel(reservationId, guestEmail));
    }

    private Reservation cancel(UUID reservationId, String guestEmail) {
        Reservation reservation = queries.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));
        if (guestEmail == null || !reservation.guest().email().equals(guestEmail.trim().toLowerCase(Locale.ROOT))) {
            throw new ReservationAccessDeniedException();
        }
        Reservation cancelled = reservation.cancel(LocalDate.now(clock));
        inventory.release(reservation.roomTypeId(), reservation.stay(), reservation.rooms());
        return reservations.save(cancelled);
    }
}

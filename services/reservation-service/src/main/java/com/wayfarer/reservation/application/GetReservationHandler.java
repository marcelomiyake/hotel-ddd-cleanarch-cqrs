package com.wayfarer.reservation.application;

import com.wayfarer.reservation.domain.Reservation;
import com.wayfarer.reservation.domain.ReservationAccessDeniedException;
import com.wayfarer.reservation.domain.ReservationNotFoundException;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public final class GetReservationHandler {
    private final ReservationQueries queries;

    public GetReservationHandler(ReservationQueries queries) {
        this.queries = Objects.requireNonNull(queries);
    }

    public Reservation handle(UUID id, String guestEmail) {
        Reservation reservation = queries.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new ReservationNotFoundException(id));
        if (guestEmail == null || !reservation.guest().email().equals(guestEmail.trim().toLowerCase(Locale.ROOT))) {
            throw new ReservationAccessDeniedException();
        }
        return reservation;
    }
}

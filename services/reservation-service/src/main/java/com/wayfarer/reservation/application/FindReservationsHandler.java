package com.wayfarer.reservation.application;

import com.wayfarer.reservation.domain.DomainRuleViolation;
import com.wayfarer.reservation.domain.Reservation;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class FindReservationsHandler {
    private final ReservationQueries queries;

    public FindReservationsHandler(ReservationQueries queries) {
        this.queries = Objects.requireNonNull(queries);
    }

    public List<Reservation> handle(String guestEmail) {
        if (guestEmail == null || guestEmail.isBlank()) {
            throw new DomainRuleViolation("A guest email is required to find reservations.");
        }
        return List.copyOf(queries.findByGuestEmail(guestEmail.trim().toLowerCase(Locale.ROOT)));
    }
}

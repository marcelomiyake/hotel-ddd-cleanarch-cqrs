package com.wayfarer.reservation.domain;

import java.util.UUID;

public final class ReservationNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ReservationNotFoundException(UUID id) {
        super("Reservation '" + id + "' was not found.");
    }
}
